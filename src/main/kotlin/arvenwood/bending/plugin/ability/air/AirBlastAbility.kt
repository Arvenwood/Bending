package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LeftClick
import arvenwood.bending.api.ability.AbilityExecutionType.Sneak
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.affectedEntities
import arvenwood.bending.api.ability.StandardContext.affectedLocations
import arvenwood.bending.api.ability.StandardContext.currentLocation
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.action.ParticleProjectile
import com.flowpowered.math.vector.Vector3d
import kotlinx.coroutines.Job
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class AirBlastAbility(
    override val cooldown: Long,
    val range: Double,
    val speed: Double,
    val radius: Double,
    val damage: Double,
    val pushFactor: Double,
    val pushFactorOther: Double,
    val canFlickLevers: Boolean,
    val canOpenDoors: Boolean,
    val canPressButtons: Boolean,
    val canCoolLava: Boolean,
    val numParticles: Int,
    val selectRange: Double
) : Ability<AirBlastAbility> {

    override val type: AbilityType<AirBlastAbility> get() = AirBlastAbility

    companion object : AbstractAbilityType<AirBlastAbility>(
        element = Elements.Air,
        executionTypes = setOf(LeftClick::class, Sneak::class),
        id = "bending:air_blast",
        name = "AirBlast"
    ) {
        override val default: Ability<AirBlastAbility> = AirBlastAbility(
            cooldown = 600L,
            range = 20.0,
            speed = 25.0,
            radius = 2.0,
            damage = 0.0,
            pushFactor = 2.0,
            pushFactorOther = 1.6,
            canFlickLevers = true,
            canOpenDoors = true,
            canPressButtons = true,
            canCoolLava = true,
            numParticles = 6,
            selectRange = 10.0
        )

        override fun load(node: ConfigurationNode): AirBlastAbility = AirBlastAbility(
            cooldown = node.getNode("cooldown").long,
            range = node.getNode("range").double,
            speed = node.getNode("speed").double,
            radius = node.getNode("radius").double,
            damage = node.getNode("damage").double,
            pushFactor = node.getNode("pushFactor").double,
            pushFactorOther = node.getNode("pushFactorOther").double,
            canFlickLevers = node.getNode("canFlickLevers").boolean,
            canOpenDoors = node.getNode("canOpenDoors").boolean,
            canPressButtons = node.getNode("canPressButtons").boolean,
            canCoolLava = node.getNode("canCoolLava").boolean,
            numParticles = node.getNode("numParticles").int,
            selectRange = node.getNode("selectRange").double
        )

        @JvmStatic
        internal val EXTINGUISH_EFFECT: ParticleEffect = ParticleEffect.builder().type(ParticleTypes.FIRE_SMOKE).build()
    }

    private val speedFactor: Double = this.speed * (50 / 1000.0)

    private val random: Random = java.util.Random().asKotlinRandom()

    override fun prepare(player: Player, context: AbilityContext) {
        context[affectedLocations] = HashSet()
        context[affectedEntities] = HashSet()
        context[origin] = player.eyeLocation
        context[direction] = player.headDirection.normalize()
        context[currentLocation] = player.eyeLocation
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player = context[player] ?: return ErrorNoTarget

        return when (executionType) {
            LeftClick -> this.runLeftClickMode(context, player, false)
            Sneak -> this.runSneakMode(context, player)
            else -> Success
        }
    }

    private suspend fun runSneakMode(context: AbilityContext, player: Player): AbilityResult {
        val origin: Location<World> = player.getTargetLocation(this.selectRange)

        context[StandardContext.origin] = origin

        val defer: Job = BenderService.get()[player.uniqueId].deferExecution(AirBlastAbility, LeftClick)
        abilityLoop {
            if (player.isRemoved) {
                defer.cancel()
                return ErrorDied
            }
            if (origin.distanceSquared(player.eyeLocation) > this.selectRange * this.selectRange) {
                defer.cancel()
                return Success
            }

            origin.spawnParticles(EffectService.get().createRandomParticle(Elements.Air, 4))

            if (defer.isCompleted) {
                context[direction] = player.headDirection.normalize()

                return this.runLeftClickMode(context, player, true)
            }
        }

        return Success
    }

    private suspend fun runLeftClickMode(context: AbilityContext, player: Player, fromAlternate: Boolean): AbilityResult {
        if (player.eyeLocation.blockType.isLiquid()) return ErrorUnderWater

        val affectedLocations: MutableCollection<Location<World>> = context.require(affectedLocations)
        val affectedEntities: MutableCollection<Entity> = context.require(affectedEntities)
        val origin: Location<World> = context.require(origin)
        val direction: Vector3d = context.require(direction)

        val projectile = ParticleProjectile(origin, direction, this.speed, this.range, true)
        abilityLoop {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }

            val result: AbilityResult = projectile.advance {
                ParticleProjectile.affectBlocks(it, this.radius, player, affectedLocations)

                ParticleProjectile.affectEntities(
                    it, player, origin, direction,
                    affectedEntities, fromAlternate,
                    this.radius, this.pushFactor, this.pushFactorOther, this.speed, this.speedFactor, this.range, this.damage
                )

                if (it !in affectedLocations && it.blockType.isSolid() || it.blockType.isLiquid()) {
                    if (this.canCoolLava && it.blockType == BlockTypes.LAVA || it.blockType == BlockTypes.FLOWING_LAVA) {
                        when {
                            it.blockType == BlockTypes.FLOWING_LAVA -> it.blockType = BlockTypes.AIR
                            it.get(Keys.FLUID_LEVEL).get() == 0 -> it.blockType = BlockTypes.OBSIDIAN
                            else -> it.blockType = BlockTypes.COBBLESTONE
                        }
                    }
                    return Success
                }

                // Show the particles.
                it.spawnParticles(EffectService.get().createParticle(Elements.Air, this.numParticles, AirConstants.VECTOR_0_275))

                if (this.random.nextInt(4) == 0) {
                    // Play the sounds every now and then.
                    it.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, it.position, 0.5, 1.0)
                }
            }

            if (result != Success) {
                return result
            }
        }

        return Success
    }
}