package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionTypes.FALL
import arvenwood.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.action.AirProjectile
import arvenwood.bending.plugin.action.advanceAll
import arvenwood.bending.plugin.util.forExclusive
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

data class AirBurstAbility(
    override val cooldown: Long,
    val chargeTime: Long,
    val blastRadius: Double,
    val damage: Double,
    val pushFactor: Double,
    val range: Double,
    val speed: Double,
    val fallThreshold: Double,
    val numSneakParticles: Int,
    val angleTheta: Double,
    val anglePhi: Double,
    val maxConeDegrees: Double = 30.0
) : Ability<AirBurstAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        chargeTime = node.getNode("chargeTime").long,
        blastRadius = node.getNode("blastRadius").double,
        damage = node.getNode("damage").double,
        pushFactor = node.getNode("pushFactor").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        fallThreshold = node.getNode("fallThreshold").double,
        numSneakParticles = node.getNode("numSneakParticles").int,
        angleTheta = node.getNode("angleTheta").double,
        anglePhi = node.getNode("anglePhi").double
    )

    override val type: AbilityType<AirBurstAbility> = AbilityTypes.AIR_BURST

    private val particleEffect: ParticleEffect =
        EffectService.get().createParticle(Elements.AIR, this.numSneakParticles, AirConstants.VECTOR_0_275)

    private val maxConeRadians: Double = Math.toRadians(this.maxConeDegrees)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        when (executionType) {
            FALL -> {
                if (context.require(StandardContext.fallDistance) >= this.fallThreshold) {
                    return this.burst(player, player.location, 75.0, 105.0)
                } else {
                    return Success
                }
            }
            LEFT_CLICK -> {
                return this.burst(player, player.location, 0.0, 180.0, player.headDirection.normalize(), this.maxConeRadians)
            }
            else -> {
                var charged = false
                val startTime: Long = System.currentTimeMillis()
                abilityLoopUnsafe {
                    if (!charged && startTime + this.chargeTime <= System.currentTimeMillis()) {
                        charged = true
                    }

                    if (!player.isSneaking) {
                        return if (charged) this.burst(player, player.eyeLocation, 0.0, 180.0) else Success
                    }

                    if (charged) {
                        player.eyeLocation.spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, this.numSneakParticles))
                    } else {
                        player.eyeLocation.spawnParticles(AirConstants.EXTINGUISH_EFFECT)
                    }
                }
            }
        }
    }

    private suspend fun burst(
        source: Player, origin: Location<World>,
        thetaMin: Double, thetaMax: Double,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ): AbilityResult {
        val projectiles: List<AirProjectile> = createProjectiles(origin, thetaMin, thetaMax, targetDirection, maxAngle)

        val affectedLocations = ArrayList<Location<World>>()
        val affectedEntities = ArrayList<Entity>()
        abilityLoopUnsafe {
            val anySucceeded: Boolean = projectiles.advanceAll { projectile: AirProjectile, _: Location<World> ->
                projectile.affectBlocks(source, affectedLocations)
                projectile.affectEntities(source, affectedEntities, false)
                projectile.visualize(this.particleEffect, Constants.RANDOM.nextInt(9) == 0)
            }

            if (!anySucceeded) {
                return Success
            }
        }
    }

    private fun createProjectiles(
        origin: Location<World>,
        thetaMin: Double, thetaMax: Double,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ): List<AirProjectile> {
        val result = ArrayList<AirProjectile>()

        forInclusive(from = thetaMin, to = thetaMax, step = this.angleTheta) { theta: Double ->
            val sinTheta: Double = sin(Math.toRadians(theta))
            val cosTheta: Double = cos(Math.toRadians(theta))

            val deltaPhi: Double = this.anglePhi / sin(Math.toRadians(theta))
            forExclusive(from = 0.0, to = 360.0, step = deltaPhi) { phi: Double ->
                val sinPhi: Double = sin(Math.toRadians(phi))
                val cosPhi: Double = cos(Math.toRadians(phi))

                val x: Double = cosPhi * sinTheta
                val y: Double = sinPhi * sinTheta
                val z: Double = cosTheta

                val direction = Vector3d(x, y, z)

                if (maxAngle > 0 && direction.angle(targetDirection) > maxAngle) {
                    return@forExclusive
                }

                result += AirProjectile(
                    origin = origin,
                    direction = direction,
                    damage = this.damage,
                    pushFactorSelf = this.pushFactor,
                    pushFactorOther = this.pushFactor,
                    radius = this.blastRadius,
                    range = this.range,
                    speed = this.speed,
                    checkDiagonals = true,
                    canExtinguishFlames = true,
                    canCoolLava = false
                )
            }
        }

        return result
    }
}