package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.service.ProtectionService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class AirShieldAbility(
    override val cooldown: Long,
    val duration: Long,
    val speed: Double,
    val maxRadius: Double,
    val initialRadius: Double,
    val numStreams: Int,
    val particles: Int
) : Ability<AirShieldAbility> {

    override val type: AbilityType<AirShieldAbility> get() = AirShieldAbility

    companion object : AbstractAbilityType<AirShieldAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(AbilityExecutionType.SNEAK),
        id = "bending:air_shield",
        name = "AirShield"
    ) {
        override val default: Ability<AirShieldAbility> = AirShieldAbility(
            cooldown = 0L,
            duration = 0L,
            speed = 10.0,
            maxRadius = 7.0,
            initialRadius = 1.0,
            numStreams = 5,
            particles = 5
        )

        override fun load(node: ConfigurationNode): AirShieldAbility = AirShieldAbility(
            cooldown = node.getNode("cooldown").long,
            duration = node.getNode("duration").long,
            speed = node.getNode("speed").double,
            maxRadius = node.getNode("maxRadius").double,
            initialRadius = node.getNode("initialRadius").double,
            numStreams = node.getNode("numStreams").int,
            particles = node.getNode("particles").int
        )

        @JvmStatic
        private fun createAngleDegMap(maxRadius: Double, numStreams: Int): Map<Int, Int> {
            val angles = HashMap<Int, Int>()
            var angle = 0
            val di: Int = (maxRadius * 2 / numStreams).toInt()
            for (i: Int in -maxRadius.toInt() + di until maxRadius.toInt() step di) {
                angles[i] = angle
                angle += 90
                if (angle == 360) angle = 0
            }
            return angles
        }

        private val COS_50_DEG: Double = cos(Math.toRadians(50.0))
        private val SIN_50_DEG: Double = sin(Math.toRadians(50.0))
    }

    private val random: Random = java.util.Random().asKotlinRandom()

    private val speedRadians: Double = Math.toRadians(this.speed)

    private val angleDegMap: Map<Int, Int> = createAngleDegMap(this.maxRadius, this.numStreams)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[player] ?: return ErrorNoTarget

        val startTime: Long = System.currentTimeMillis()

        var radius: Double by context.by(StandardContext.radius, this.initialRadius)

        val angles: MutableMap<Int, Int> = this.angleDegMap.toMap(HashMap())

        abilityLoopUnsafe {
            if (player.eyeLocation.blockType.isLiquid()) {
                return ErrorUnderWater
            } else if (!player.getOrElse(Keys.IS_SNEAKING, false)) {
                return Success
            } else if (this.duration > 0 && startTime + this.duration <= System.currentTimeMillis()) {
                return Success
            }

            rotateShield(context, angles)
        }
    }

    private fun rotateShield(context: AbilityContext, angles: MutableMap<Int, Int>) {
        val player: Player = context.require(player)
        val origin: Location<World> = player.location

        var radius: Double by context.by(StandardContext.radius)

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
                test.spawnParticles(AirBlastAbility.EXTINGUISH_EFFECT)
            }
        }

        for ((index, angle) in angles) {
            val rAngle: Double = Math.toRadians(angle.toDouble())

            val factor: Double = radius / this.maxRadius
            val f: Double = sqrt(1 - factor * factor * (index / radius) * (index / radius))

            val x: Double = origin.x + radius * cos(rAngle) * f
            val y: Double = origin.y + factor * index
            val z: Double = origin.z + radius * sin(rAngle) * f

            val effect: Location<World> = origin.setPosition(Vector3d(x, y, z))
            if (!BuildProtectionService.get().isProtected(player, effect)) {
                effect.spawnParticles(EffectService.get().createRandomParticle(Elements.Air, this.particles))
                if (this.random.nextInt(4) == 0) {
                    effect.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, effect.position, 0.5, 1.0)
                }
            }

            angles[index] = angle + this.speed.toInt()
        }

        if (radius < this.maxRadius) {
            // Kotlin doesn't like operator assignment for delegates
            @Suppress("ReplaceWithOperatorAssignment")
            radius = radius + 0.3
        }
        if (radius > this.maxRadius) {
            radius = this.maxRadius
        }
    }
}