package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityExecutionType.SNEAK
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.affectedEntities
import arvenwood.bending.api.ability.StandardContext.affectedLocations
import arvenwood.bending.api.ability.StandardContext.currentLocation
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.action.AirProjectile
import com.flowpowered.math.vector.Vector3d
import kotlinx.coroutines.Job
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

data class AirBlastAbility(
    override val cooldown: Long,
    val range: Double,
    val speed: Double,
    val radius: Double,
    val damage: Double,
    val pushFactorSelf: Double,
    val pushFactorOther: Double,
    val canFlickLevers: Boolean,
    val canOpenDoors: Boolean,
    val canPressButtons: Boolean,
    val canCoolLava: Boolean,
    val numParticles: Int,
    val selectRange: Double
) : Ability<AirBlastAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        radius = node.getNode("radius").double,
        damage = node.getNode("damage").double,
        pushFactorSelf = node.getNode("pushFactor").double,
        pushFactorOther = node.getNode("pushFactorOther").double,
        canFlickLevers = node.getNode("canFlickLevers").boolean,
        canOpenDoors = node.getNode("canOpenDoors").boolean,
        canPressButtons = node.getNode("canPressButtons").boolean,
        canCoolLava = node.getNode("canCoolLava").boolean,
        numParticles = node.getNode("numParticles").int,
        selectRange = node.getNode("selectRange").double
    )

    override val type: AbilityType<AirBlastAbility> = AbilityTypes.AIR_BLAST

    private val particleEffect: ParticleEffect =
        EffectService.get().createParticle(Elements.AIR, this.numParticles, AirConstants.VECTOR_0_275)

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
            LEFT_CLICK -> this.runLeftClickMode(context, player, false)
            SNEAK -> this.runSneakMode(context, player)
            else -> Success
        }
    }

    private suspend fun runSneakMode(context: AbilityContext, player: Player): AbilityResult {
        val origin: Location<World> = player.getTargetLocation(this.selectRange)

        context[StandardContext.origin] = origin

        val bender: Bender = context.require(StandardContext.bender)
        val defer: Job = bender.deferExecution(this.type, LEFT_CLICK)
        abilityLoop {
            if (player.isRemoved) {
                defer.cancel()
                return ErrorDied
            }
            if (origin.distanceSquared(player.eyeLocation) > this.selectRange * this.selectRange) {
                defer.cancel()
                return Success
            }

            origin.spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, 4))

            if (defer.isCompleted) {
                if (defer.isCancelled) {
                    return Success
                }

                context[direction] = player.headDirection.normalize()

                return this.runLeftClickMode(context, player, true)
            }
        }

        return Success
    }

    private suspend fun runLeftClickMode(context: AbilityContext, player: Player, canPushSelf: Boolean): AbilityResult {
        if (player.eyeLocation.blockType.isLiquid()) return ErrorUnderWater

        val affectedLocations: MutableCollection<Location<World>> = context.require(affectedLocations)
        val affectedEntities: MutableCollection<Entity> = context.require(affectedEntities)
        val origin: Location<World> = context.require(origin)
        val direction: Vector3d = context.require(direction)

        val projectile = AirProjectile(
            origin = origin,
            direction = direction,
            damage = this.damage,
            pushFactorSelf = this.pushFactorSelf,
            pushFactorOther = this.pushFactorOther,
            radius = this.radius,
            range = this.range,
            speed = this.speed,
            checkDiagonals = true,
            canExtinguishFlames = true,
            canCoolLava = this.canCoolLava
        )

        abilityLoop {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }

            val result: AbilityResult = projectile.advance {
                projectile.affectBlocks(player, affectedLocations)
                projectile.affectEntities(player, affectedEntities, canPushSelf)
                projectile.visualize(this.particleEffect, Constants.RANDOM.nextInt(4) == 0)
            }

            if (result != Success) {
                return result
            }
        }

        return Success
    }
}