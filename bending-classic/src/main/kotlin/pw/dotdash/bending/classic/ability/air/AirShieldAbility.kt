package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * TODO: fix
 */
data class AirShieldAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val speed: Double,
    val maxRadius: Double,
    val initialRadius: Double,
    val numStreams: Int,
    val particles: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_SHIELD) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        speed = node.getNode("speed").double,
        maxRadius = node.getNode("maxRadius").double,
        initialRadius = node.getNode("initialRadius").double,
        numStreams = node.getNode("numStreams").int,
        particles = node.getNode("particles").int
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val offsetSize: Int = ((this.maxRadius - this.initialRadius) / 0.3).toInt()

    private val angleDegMap: Map<Int, Int> = this.createAngleDegMap()

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)
        val origin: Location<World> = player.location

        val angles: MutableMap<Int, Int> = angleDegMap.toMap(HashMap())
        val offsets: List<List<Vector3d>> = calculateRangedOffsets(angles)

        var index = 0
        var radius: Double = initialRadius
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.eyeLocation.blockType.isLiquid()) {
                return
            } else if (!player.isSneaking) {
                return
            } else if (startTime.elapsedNow() >= duration) {
                return
            }

            for (entity: Entity in origin.getNearbyEntities(radius)) {
                if (PvpProtectionService.getInstance().isProtected(player, entity)) continue

                if (origin.distanceSquared(entity.location) > 4) {
                    val x: Double = entity.location.x - origin.x
                    val z: Double = entity.location.z - origin.z
                    val magnitude: Double = sqrt(x * x + z * z)
                    val vx: Double = (x * COS_50_DEG - z * SIN_50_DEG) / magnitude
                    val vz: Double = (x * SIN_50_DEG - z * COS_50_DEG) / magnitude

                    entity.velocity = Vector3d(vx, entity.velocity.y, vz).mul(0.5)
                    entity.offer(Keys.FALL_DISTANCE, 0F)
                }
            }

            for (test: Location<World> in origin.getNearbyLocations(radius)) {
                if (test.blockType == BlockTypes.FIRE) {
                    test.blockType = BlockTypes.AIR
                    test.spawnParticles(EffectService.getInstance().extinguishEffect)
                }
            }

            for (offset: Vector3d in offsets[index]) {
                val displayLoc: Location<World> = origin.add(offset)

                if (!BuildProtectionService.getInstance().isProtected(player, displayLoc)) {
                    displayLoc.spawnParticles(EffectService.getInstance().createRandomParticle(Elements.AIR, particles))
                    if (Random.nextInt(4) == 0) {
                        displayLoc.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, displayLoc.position, 0.5, 1.0)
                    }
                }
            }

            if (index >= offsetSize) {
                index = 0
            }

            if (radius < maxRadius) {
                radius += 0.3
            }
        }
    }

    private fun calculateRangedOffsets(angles: MutableMap<Int, Int>): List<List<Vector3d>> {
        val rangedOffsets = ArrayList<List<Vector3d>>(this.offsetSize)

        forInclusive(from = this.initialRadius, to = this.maxRadius, step = 0.3) { radius: Double ->
            rangedOffsets.add(calculateOffsets(radius, angles))
        }

        return rangedOffsets
    }

    private fun calculateOffsets(radius: Double, angles: MutableMap<Int, Int>): List<Vector3d> {
        val offsets = ArrayList<Vector3d>()

        for ((index: Int, angleDeg: Int) in angles) {
            val angleRad: Double = Math.toRadians(angleDeg.toDouble())

            val factor: Double = radius / this.maxRadius
            val f: Double = sqrt(1 - factor * factor * (index / radius) * (index / radius))

            offsets += Vector3d(
                radius * cos(angleRad) * f,
                factor * index,
                radius * sin(angleRad) * f
            )

            angles[index] = angleDeg + this.speed.toInt()
        }

        return offsets
    }

    private fun createAngleDegMap(): Map<Int, Int> {
        val angles = HashMap<Int, Int>()
        var angle = 0
        val di: Int = (2 * this.maxRadius / this.numStreams).toInt()
        for (i: Int in -this.maxRadius.toInt() + di until this.maxRadius.toInt() step di) {
            angles[i] = angle
            angle += 90
            if (angle == 360) angle = 0
        }
        return angles
    }

    companion object {
        private val COS_50_DEG: Double = cos(Math.toRadians(50.0))
        private val SIN_50_DEG: Double = sin(Math.toRadians(50.0))
    }
}