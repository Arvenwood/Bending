package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityExecutionType.SNEAK
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.action.AirProjectile
import arvenwood.bending.plugin.action.advanceAll
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class AirSwipeAbility(
    override val cooldown: Long,
    val chargeTime: Long,
    val maxChargeFactor: Double,
    val arcDegrees: Double,
    val arcIncrementDegrees: Double,
    val damage: Double,
    val pushFactor: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val numParticles: Int
) : Ability<AirSwipeAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        chargeTime = node.getNode("chargeTime").long,
        maxChargeFactor = node.getNode("maxChargeFactor").double,
        arcDegrees = node.getNode("arcDegrees").double,
        arcIncrementDegrees = node.getNode("arcIncrementDegrees").double,
        damage = node.getNode("damage").double,
        pushFactor = node.getNode("pushFactor").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        numParticles = node.getNode("numParticles").int
    )

    override val type: AbilityType<AirSwipeAbility> = AbilityTypes.AIR_SWIPE

    private val arcRadians: Double = Math.toRadians(this.arcDegrees)
    private val arcDegreesRadians: Double = Math.toRadians(this.arcIncrementDegrees)

    private val particleEffect: ParticleEffect =
        EffectService.get().createParticle(Elements.AIR, this.numParticles, AirConstants.VECTOR_0_2)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        when (executionType) {
            LEFT_CLICK -> {
                return this.swipe(player, player.eyeLocation)
            }
            else -> {
                var charged = false
                val startTime: Long = System.currentTimeMillis()
                abilityLoopUnsafe {
                    val currentTime: Long = System.currentTimeMillis()
                    if (startTime + this.chargeTime <= currentTime) {
                        charged = true
                    }

                    if (!player.isSneaking) {
                        val factor: Double = if (charged) {
                            this.maxChargeFactor
                        } else {
                            this.maxChargeFactor * (currentTime - startTime) / this.chargeTime
                        }

                        return this.swipe(player, player.eyeLocation, this.damage * factor, this.pushFactor * factor)
                    }

                    if (charged) {
                        player.eyeLocation.spawnParticles(this.particleEffect)
                    }
                }
            }
        }
    }

    private suspend fun swipe(
        source: Player, origin: Location<World>,
        damage: Double = this.damage, pushFactor: Double = this.pushFactor
    ): AbilityResult {
        val projectiles: List<AirProjectile> = createProjectiles(origin, source.headDirection.normalize(), damage, pushFactor)

        val affectedLocations = ArrayList<Location<World>>()
        val affectedEntities = ArrayList<Entity>()
        abilityLoopUnsafe {
            val anySucceeded: Boolean = projectiles.advanceAll { projectile: AirProjectile, _: Location<World> ->
                projectile.affectBlocks(source, affectedLocations)
                projectile.affectEntities(source, affectedEntities, canPushSelf = false)
                projectile.visualize(this.particleEffect, playSounds = Constants.RANDOM.nextInt(4) == 0)
            }

            if (!anySucceeded) {
                return Success
            }
        }
    }

    private fun createProjectiles(
        origin: Location<World>, direction: Vector3d,
        damage: Double = this.damage, pushFactor: Double = this.pushFactor
    ): List<AirProjectile> {
        val result = ArrayList<AirProjectile>()

        forInclusive(from = -this.arcRadians, to = this.arcRadians, step = this.arcDegreesRadians) { angle: Double ->
            val sinAngle: Double = sin(angle)
            val cosAngle: Double = cos(angle)

            val vx: Double = direction.x * cosAngle - direction.z * sinAngle
            val vz: Double = direction.x * sinAngle + direction.z * cosAngle

            result += AirProjectile(
                origin = origin,
                direction = direction.withXZ(vx, vz),
                damage = damage,
                pushFactorSelf = pushFactor,
                pushFactorOther = pushFactor,
                radius = this.radius,
                range = this.range,
                speed = this.speed,
                checkDiagonals = true,
                canExtinguishFlames = false,
                canCoolLava = false
            )
        }

        return result
    }
}