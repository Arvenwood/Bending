package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionTypes.FALL
import arvenwood.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.projectile.AirRaycast
import arvenwood.bending.plugin.raycast.Raycast
import arvenwood.bending.plugin.raycast.advanceAll
import arvenwood.bending.plugin.util.forExclusive
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
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

    private val fallDirections: List<Vector3d> = this.calculateRaycastDirections(75.0, 105.0)
    private val sphereDirections: List<Vector3d> = this.calculateRaycastDirections(0.0, 180.0)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        when (executionType) {
            FALL -> {
                if (context.require(StandardContext.fallDistance) >= this.fallThreshold) {
                    return this.burst(source = player, origin = player.location, directions = this.fallDirections)
                } else {
                    return Success
                }
            }
            LEFT_CLICK -> {
                return this.burst(
                    source = player,
                    origin = player.location,
                    directions = this.sphereDirections,
                    targetDirection = player.headDirection.normalize(),
                    maxAngle = this.maxConeRadians
                )
            }
        }

        var charged = false
        val startTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (!charged && startTime + this.chargeTime <= System.currentTimeMillis()) {
                charged = true
            }

            if (!player.isSneaking) {
                if (charged) {
                    return this.burst(source = player, origin = player.eyeLocation, directions = this.sphereDirections)
                } else {
                    return Success
                }
            }

            if (charged) {
                player.eyeLocation.spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, this.numSneakParticles))
            } else {
                player.eyeLocation.spawnParticles(AirConstants.EXTINGUISH_EFFECT)
            }
        }
    }

    private suspend fun burst(
        source: Player, origin: Location<World>, directions: List<Vector3d>,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ): AbilityResult {
        val raycasts: List<Raycast> = createRaycasts(origin, directions, targetDirection, maxAngle)

        val affectedLocations = HashSet<Location<World>>()
        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            val anySucceeded: Boolean = raycasts.advanceAll {
                if (BuildProtectionService.get().isProtected(source, it)) return@advanceAll AbilityResult.ErrorProtected

                affectLocations(source, affectedLocations, blastRadius) { test: Location<World> ->
                    AirRaycast.extinguishFlames(test)
                            || AirRaycast.toggleDoor(test)
                            || AirRaycast.toggleLever(test)
                }
                affectEntities(source, affectedEntities, blastRadius) { test: Entity ->
                    with(AirRaycast) {
                        pushEntity(source, test, false, pushFactor, pushFactor)
                    }

                    damageEntity(test, damage)
                }
                playParticles(particleEffect)

                if (Constants.RANDOM.nextInt(9) == 0) {
                    playSounds(SoundTypes.ENTITY_CREEPER_HURT, 0.5, 1.0)
                }

                return@advanceAll Success
            }

            if (!anySucceeded) {
                return Success
            }
        }
    }

    private fun createRaycasts(
        origin: Location<World>, directions: List<Vector3d>,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ): List<Raycast> {
        return directions.mapNotNull {
            if (maxAngle > 0 && it.angle(targetDirection) > maxAngle) {
                null
            } else {
                Raycast(
                    origin = origin,
                    direction = it,
                    range = this.range,
                    speed = this.speed,
                    checkDiagonals = true
                )
            }
        }
    }

    private fun calculateRaycastDirections(thetaMin: Double, thetaMax: Double): List<Vector3d> {
        val directions = ArrayList<Vector3d>()

        forInclusive(from = thetaMin, to = thetaMax, step = this.angleTheta) { theta: Double ->
            val sinTheta: Double = sin(Math.toRadians(theta))
            val cosTheta: Double = cos(Math.toRadians(theta))

            val deltaPhi: Double = this.anglePhi / sin(Math.toRadians(theta))
            forExclusive(from = 0.0, to = 360.0, step = deltaPhi) { phi: Double ->
                val sinPhi: Double = sin(Math.toRadians(phi))
                val cosPhi: Double = cos(Math.toRadians(phi))

                directions += Vector3d(
                    cosPhi * sinTheta,
                    sinPhi * sinTheta,
                    cosTheta
                )
            }
        }

        return directions
    }
}