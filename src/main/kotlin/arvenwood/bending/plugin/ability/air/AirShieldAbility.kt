package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.util.EpochTime
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * TODO: fix
 */
data class AirShieldAbility(
    override val cooldown: Long,
    val duration: Long,
    val speed: Double,
    val maxRadius: Double,
    val initialRadius: Double,
    val numStreams: Int,
    val particles: Int
) : Ability<AirShieldAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        speed = node.getNode("speed").double,
        maxRadius = node.getNode("maxRadius").double,
        initialRadius = node.getNode("initialRadius").double,
        numStreams = node.getNode("numStreams").int,
        particles = node.getNode("particles").int
    )

    override val type: AbilityType<AirShieldAbility> = AbilityTypes.AIR_SHIELD

    private val offsetSize: Int = ((this.maxRadius - this.initialRadius) / 0.3).toInt()

    private val angleDegMap: Map<Int, Int> = this.createAngleDegMap()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.player
        val origin: Location<World> = player.location

        val angles: MutableMap<Int, Int> = this.angleDegMap.toMap(HashMap())
        val offsets: List<List<Vector3d>> = this.calculateRangedOffsets(angles)

        var index = 0
        var radius: Double = this.initialRadius
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.eyeLocation.blockType.isLiquid()) {
                return ErrorUnderWater
            } else if (!player.isSneaking) {
                return Success
            } else if (startTime.elapsedNow() >= this.duration) {
                return Success
            }

            for (entity: Entity in origin.getNearbyEntities(radius)) {
                if (PvpProtectionService.get().isProtected(player, entity)) continue

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
                    test.spawnParticles(AirConstants.EXTINGUISH_EFFECT)
                }
            }

            for (offset: Vector3d in offsets[index]) {
                val displayLoc: Location<World> = origin.add(offset)

                if (!BuildProtectionService.get().isProtected(player, displayLoc)) {
                    displayLoc.spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, this.particles))
                    if (Constants.RANDOM.nextInt(4) == 0) {
                        displayLoc.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, displayLoc.position, 0.5, 1.0)
                    }
                }
            }

            if (index >= this.offsetSize) {
                index = 0
            }

            if (radius < this.maxRadius) {
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