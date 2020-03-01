package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.matrix.Matrix3d
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.ray.FastRaycast
import pw.dotdash.bending.api.ray.advanceAll
import pw.dotdash.bending.api.ray.pushEntity
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AirSwipeAbility(
    override val cooldownMilli: Long,
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
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_SWIPE) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
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

    private val arcRadians: Double = Math.toRadians(this.arcDegrees)
    private val arcIncrementRadians: Double = Math.toRadians(this.arcIncrementDegrees)

    private val particleEffect: ParticleEffect = EffectService.getInstance().createParticle(Elements.AIR, this.numParticles, VectorUtil.VECTOR_0_2)

    private val transformationMatrices: List<Matrix3d> = this.createTransformationMatrices()

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        when (executionType) {
            LEFT_CLICK -> {
                this.swipe(player, player.eyeLocation, damage, pushFactor)
            }
            else -> {
                var charged = false
                val startTime: EpochTime = EpochTime.now()

                abilityLoopUnsafe {
                    val currentTime: EpochTime = EpochTime.now()

                    if (startTime.elapsed(currentTime) >= chargeTime) {
                        charged = true
                    }

                    if (!player.isSneaking) {
                        val factor: Double = if (charged) {
                            maxChargeFactor
                        } else {
                            maxChargeFactor * (startTime.elapsed(currentTime)) / chargeTime
                        }

                        return this.swipe(player, player.eyeLocation, damage * factor, pushFactor * factor)
                    }

                    if (charged) {
                        player.eyeLocation.spawnParticles(particleEffect)
                    }
                }
            }
        }
    }

    private suspend fun CoroutineTask.swipe(source: Player, origin: Location<World>, damage: Double, pushFactor: Double) {
        val raycasts: List<FastRaycast> = createRaycasts(origin, source.headDirection.normalize())

        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            val anySucceeded: Boolean = raycasts.advanceAll {
                if (BuildProtectionService.getInstance().isProtected(source, it)) {
                    return@advanceAll false
                }

                affectEntities(source, affectedEntities, radius) { test: Entity ->
                    pushEntity(source, test, false, pushFactor, pushFactor)

                    damageEntity(test, damage)
                }
                playParticles(particleEffect)

                if (Random.nextInt(4) == 0) {
                    playSounds(SoundTypes.ENTITY_CREEPER_HURT, 0.5, 1.0)
                }

                return@advanceAll true
            }

            if (!anySucceeded) {
                return
            }
        }
    }

    private fun createRaycasts(origin: Location<World>, direction: Vector3d): List<FastRaycast> =
        this.transformationMatrices.map { matrix: Matrix3d ->
            FastRaycast(
                origin = origin,
                direction = matrix.transform(direction),
                range = this.range,
                speed = this.speed,
                checkDiagonals = true
            )
        }

    private fun createTransformationMatrices(): List<Matrix3d> {
        val matrices = ArrayList<Matrix3d>()

        forInclusive(from = -this.arcRadians, to = this.arcRadians, step = this.arcIncrementRadians) { angle: Double ->
            val sinAngle: Double = sin(angle)
            val cosAngle: Double = cos(angle)

            matrices += Matrix3d(
                cosAngle, 0.0, -sinAngle,
                0.0, 1.0, 0.0,
                sinAngle, 0.0, cosAngle
            )
        }

        return matrices
    }
}