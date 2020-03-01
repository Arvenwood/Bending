package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * TODO make it work
 */
data class AirTornadoAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val maxHeight: Double,
    val pushFactor: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val particles: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_TORNADO) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        maxHeight = node.getNode("maxHeight").double,
        pushFactor = node.getNode("pushFactor").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        particles = node.getNode("particles").int
    )

    private val numStreams: Int = (this.maxHeight * 0.3).toInt()
    private val angleDegMap: Map<Int, Int> = createAngleDegMap()

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)
        val angles: MutableMap<Int, Int> = angleDegMap.toMutableMap()

        var currentHeight = 2.0
        var currentRadius: Double

        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.isRemoved) {
                return
            }
            if (!player.isSneaking) {
                return
            }
            if (player.eyeLocation.blockType.isLiquid()) {
                return
            }
            if (startTime.elapsedNow() >= duration) {
                return
            }

            var origin: Location<World> = player.getTargetLocation(radius) { true }

            val timeFactor: Double = currentHeight / maxHeight
            currentRadius = timeFactor * radius

            if (origin.blockType != BlockTypes.AIR && origin.blockType != BlockTypes.BARRIER) {
                origin = origin.setY(origin.y - 1.0 / 10.0 * currentHeight)

                for (entity: Entity in origin.getNearbyEntities(currentHeight)) {
                    if (PvpProtectionService.getInstance().isProtected(player, entity)) continue

                    val y: Double = entity.location.y
                    if (y > origin.y && y < origin.y + currentHeight) {
                        val factor: Double = (y - origin.y) / currentHeight
                        val test: Location<World> = origin.setY(y)

                        if (test.distance(entity.location) < currentRadius * factor) {
                            val x: Double = entity.location.x - origin.x
                            val z: Double = entity.location.z - origin.z
                            val magnitude: Double = sqrt(x * x + z * z)

                            var vx: Double = 0.0
                            var vy: Double = 0.7 * pushFactor
                            var vz: Double = 0.0

                            if (magnitude != 0.0) {
                                vx = (x * COS_100_DEG - z * SIN_100_DEG) / magnitude
                                vz = (x * SIN_100_DEG + z * COS_100_DEG) / magnitude
                            }

                            if (entity.uniqueId == player.uniqueId) {
                                val direction: Vector3d = player.headDirection.normalize()
                                vx = direction.x
                                vz = direction.z
                                val dy: Double = player.location.y - origin.y

                                vy = when {
                                    dy >= currentHeight * 0.95 -> 0.0
                                    dy >= currentHeight * 0.85 -> 6.0 * (0.95 - dy / currentHeight)
                                    else -> 0.6
                                }
                            }

                            entity.velocity = Vector3d(vx, vy, vz).mul(timeFactor)
                            entity.offer(Keys.FALL_DISTANCE, 0F)
                        }
                    }
                }

                for ((i: Int, angle: Int) in angles) {
                    val rAngle: Double = Math.toRadians(angle.toDouble())
                    val factor: Double = i / currentHeight
                    val alpha: Double = timeFactor * factor * currentRadius

                    val x: Double = origin.x + alpha * cos(rAngle)
                    val y: Double = origin.y + timeFactor * i
                    val z: Double = origin.z + alpha * sin(rAngle)

                    val effect: Location<World> = Location(origin.extent, x, y, z)

                    if (!BuildProtectionService.getInstance().isProtected(player, effect)) {
                        EffectService.getInstance().createRandomParticle(Elements.AIR, particles)
                        if (Random.nextInt(20) == 0) {
                            // Play the sounds every now and then.
                            effect.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, effect.position, 0.5, 1.0)
                        }
                    }

                    angles[i] = angle + 25 * speed.toInt()
                }
            }

            currentHeight = if (currentHeight > maxHeight) maxHeight else currentHeight + 1
        }
    }

    private fun createAngleDegMap(): MutableMap<Int, Int> {
        val angles = HashMap<Int, Int>()
        var angle = 0
        var i = 0
        val di: Int = (this.maxHeight / this.numStreams).toInt()
        while (i <= this.maxHeight) {
            angles[i] = angle
            angle += 90
            if (angle == 360) angle = 0

            i += di
        }
        return angles
    }

    companion object {
        private val DEG_100_IN_RAD: Double = Math.toRadians(100.0)
        private val SIN_100_DEG: Double = sin(DEG_100_IN_RAD)
        private val COS_100_DEG: Double = cos(DEG_100_IN_RAD)
    }
}