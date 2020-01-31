package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.affectedEntities
import arvenwood.bending.api.ability.StandardContext.affectedLocations
import arvenwood.bending.api.ability.StandardContext.currentLocation
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.service.ProtectionService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.absoluteValue
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
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK, AbilityExecutionType.SNEAK),
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
            range = node.getNode("radius").double,
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

        @JvmStatic
        private fun doorSound(open: Boolean): SoundType =
            if (open) SoundTypes.BLOCK_WOODEN_DOOR_OPEN else SoundTypes.BLOCK_WOODEN_DOOR_CLOSE

        private val PARTICLE_OFFSET = Vector3d(0.275, 0.275, 0.275)
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
            AbilityExecutionType.LEFT_CLICK -> runLeftClickMode(context, player, false)
            AbilityExecutionType.SNEAK -> runSneakMode(context, player)
            else -> Success
        }
    }

    private suspend fun runSneakMode(context: AbilityContext, player: Player): AbilityResult {
        val origin: Location<World> = player.getTargetLocation(this.selectRange)

        context[StandardContext.origin] = origin
        context[currentLocation] = origin

        val defer: Job = BenderService.get()[player.uniqueId].deferExecution(AirBlastAbility, AbilityExecutionType.LEFT_CLICK)
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

                return runLeftClickMode(context, player, true)
            }
        }

        return Success
    }

    private suspend fun runLeftClickMode(context: AbilityContext, player: Player, fromAlternate: Boolean): AbilityResult {
        if (player.eyeLocation.blockType.isLiquid()) return ErrorUnderWater

        var location: Location<World> by context.by(currentLocation)
        val affected: MutableCollection<Location<World>> by context.by(affectedLocations)
        val origin: Location<World> = context.require(origin)
        val direction: Vector3d = context.require(direction)

        abilityLoop {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }

            for (test: Location<World> in location.getNearbyLocations(radius)) {
                if (BuildProtectionService.get().isProtected(player, test)) {
                    // Can't fight here!
                    continue
                }

                if (test.blockType == BlockTypes.FIRE) {
                    // Extinguish flames.
                    test.blockType = BlockTypes.AIR
                    test.extent.spawnParticles(EXTINGUISH_EFFECT, test.position)
                    continue
                }

                if (test in affected) {
                    // We've already opened this door, flicked this lever, etc.
                    continue
                }

                if (test.blockType in AirConstants.DOORS) {
                    // Open/Close doors.
                    val open: Boolean = test.get(Keys.OPEN).orElse(false)
                    test.offer(Keys.OPEN, !open)
                    test.extent.playSound(doorSound(!open), test.position, 0.5, 0.0)
                    affected += test
                } else if (test.blockType == BlockTypes.LEVER) {
                    // Flip switches.
                    test.offer(Keys.POWERED, test.get(Keys.POWERED).orElse(false))
                    test.extent.playSound(SoundTypes.BLOCK_LEVER_CLICK, test.position, 0.5, 0.0)
                    affected += test
                }
            }

            if (location !in affected && location.blockType.isSolid() || location.blockType.isLiquid()) {
                if (this.canCoolLava && location.blockType == BlockTypes.LAVA || location.blockType == BlockTypes.FLOWING_LAVA) {
                    when {
                        location.blockType == BlockTypes.FLOWING_LAVA -> location.blockType = BlockTypes.AIR
                        location.get(Keys.FLUID_LEVEL).get() == 0 -> location.blockType = BlockTypes.OBSIDIAN
                        else -> location.blockType = BlockTypes.COBBLESTONE
                    }
                }
                return Success
            }

            val distance: Double = location.distanceSquared(origin)
            if (distance > this.range * this.range) {
                // Reached our limit!
                return Success
            }

            for (entity: Entity in location.getNearbyEntities(radius)) {
                if (PvpProtectionService.get().isProtected(player, entity)) {
                    // Can't fight here!
                    continue
                }

                // Push the entity around.
                affect(context, entity, fromAlternate)
            }

            // Show the particles.
            location.spawnParticles(EffectService.get().createParticle(Elements.Air, this.numParticles, PARTICLE_OFFSET))

            if (this.random.nextInt(4) == 0) {
                // Play the sounds every now and then.
                location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, location.position, 0.5, 1.0)
            }

            if (location.isNearDiagonalWall(direction)) {
                // Stop if we've hit a diagonal wall.
                return Success
            }

            // Move forward.
            location = location.add(direction.mul(this.speedFactor))
        }

        return Success
    }

    /**
     * Pushes and damages the given entity.
     */
    private fun affect(context: AbilityContext, entity: Entity, fromAlternate: Boolean) {
//        if (entity is Player && entity.getOrElse(BendingKeys.INVINCIBLE, false)) {
//            // Ignore them if they're invincible.
//            return
//        }

        val player = context.require(player)

        val isSelf = entity.uniqueId == player.uniqueId
        var knockback = this.pushFactorOther

        if (isSelf) {
            if (fromAlternate) {
                knockback = this.pushFactor
            } else {
                // Ignore us.
                return
            }
        }

        val max: Double = this.speed / this.speedFactor

        var push = context.require(direction)
        if (push.y.absoluteValue > max && !isSelf) {
            push = push.withY(if (push.y < 0) -max else max)
        }

        knockback *= (1 - entity.position.distance(context.require(origin).position) / (2 * this.range))

        if (entity.location.add(0.0, -0.5, 0.0).blockType.isSolid()) {
            knockback *= 0.85
        }

        push = push.normalize().mul(knockback)
        if (entity.velocity.dot(push).absoluteValue > knockback && entity.velocity.angle(push) > Math.PI / 3) {
            // Increase the velocity in their current direction.
            push = push.normalize().add(entity.velocity).mul(knockback)
        }

        // Toss the entity.
        entity.velocity = push.min(4.0, 4.0, 4.0).max(-4.0, -4.0, -4.0)

        val affected by context.by(affectedEntities)
        if (this.damage > 0 && entity is Living && entity.uniqueId != player.uniqueId && entity !in affected) {
            // Hurt them.
            entity.damage(this.damage, DamageSources.MAGIC)
            affected.add(entity)
        }

        if (entity.get(Keys.FIRE_TICKS).orElse(0) > 0) {
            // Make the entity stop, drop, and roll.
            entity.offer(Keys.FIRE_TICKS, 0)
            entity.world.spawnParticles(EXTINGUISH_EFFECT, entity.position)
        }
    }
}