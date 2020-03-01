package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.FALL_DISTANCE
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.FALL
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.ray.AirRaycast
import pw.dotdash.bending.api.ray.FastRaycast
import pw.dotdash.bending.api.ray.progressAll
import pw.dotdash.bending.api.ray.pushEntity
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AirBurstAbility(
    override val cooldownMilli: Long,
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
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_BURST) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
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

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val particleEffect: ParticleEffect =
        EffectService.getInstance().createParticle(Elements.AIR, this.numSneakParticles, VectorUtil.VECTOR_0_275)

    private val maxConeRadians: Double = Math.toRadians(this.maxConeDegrees)

    private val fallDirections: Array<Vector3d> = this.calculateRaycastDirections(75.0, 105.0)
    private val sphereDirections: Array<Vector3d> = this.calculateRaycastDirections(0.0, 180.0)

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        when (executionType) {
            FALL -> {
                if (context.require(FALL_DISTANCE) >= fallThreshold) {
                    this.burst(source = player, origin = player.location, directions = fallDirections)
                }
                return
            }
            LEFT_CLICK -> {
                this.burst(
                    source = player,
                    origin = player.eyeLocation,
                    directions = sphereDirections,
                    targetDirection = player.headDirection.normalize(),
                    maxAngle = maxConeRadians
                )
                return
            }
        }

        var charged = false
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (!charged && startTime.elapsedNow() >= chargeTime) {
                charged = true
            }

            if (!player.isSneaking) {
                if (charged) {
                    this.burst(source = player, origin = player.eyeLocation, directions = sphereDirections)
                    return
                } else {
                    return
                }
            }

            if (charged) {
                player.eyeLocation.spawnParticles(EffectService.getInstance().createRandomParticle(Elements.AIR, numSneakParticles))
            } else {
                player.eyeLocation.spawnParticles(EffectService.getInstance().extinguishEffect)
            }
        }
    }

    private suspend fun CoroutineTask.burst(
        source: Player, origin: Location<World>, directions: Array<Vector3d>,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ) {
        val raycasts: List<FastRaycast> = createRaycasts(origin, directions, targetDirection, maxAngle)

        val affectedLocations = HashSet<Location<World>>()
        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            if (source.isRemoved) {
                return
            }

            val anySucceeded: Boolean = raycasts.progressAll {
                if (BuildProtectionService.getInstance().isProtected(source, it)) return@progressAll false

                affectLocations(source, affectedLocations, blastRadius) { test: Location<World> ->
                    AirRaycast.extinguishFlames(test)
                            || AirRaycast.toggleDoor(test)
                            || AirRaycast.toggleLever(test)
                }
                affectEntities(source, affectedEntities, blastRadius) { test: Entity ->
                    pushEntity(source, test, false, pushFactor, pushFactor)

                    damageEntity(test, damage)
                }
                playParticles(particleEffect)

                if (Random.nextInt(9) == 0) {
                    playSounds(SoundTypes.ENTITY_CREEPER_HURT, 0.5, 1.0)
                }

                return@progressAll true
            }

            if (!anySucceeded) {
                return
            }
        }
    }

    private fun createRaycasts(
        origin: Location<World>, directions: Array<Vector3d>,
        targetDirection: Vector3d = Vector3d.ZERO, maxAngle: Double = 0.0
    ): List<FastRaycast> {
        return directions.mapNotNull {
            if (maxAngle > 0 && it.angle(targetDirection) > maxAngle) {
                null
            } else {
                FastRaycast(
                    origin = origin,
                    direction = it,
                    range = this.range,
                    speed = this.speed,
                    checkDiagonals = true
                )
            }
        }
    }

    private fun calculateRaycastDirections(thetaMin: Double, thetaMax: Double): Array<Vector3d> {
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

        return directions.toTypedArray()
    }
}