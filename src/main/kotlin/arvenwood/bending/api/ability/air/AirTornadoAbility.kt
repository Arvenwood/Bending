package arvenwood.bending.api.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
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

data class AirTornadoAbility(
    override val cooldown: Long,
    val duration: Long,
    val height: Double,
    val pushFactor: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val particles: Int
) : Ability<AirTornadoAbility> {

    override val type: AbilityType<AirTornadoAbility> = AirTornadoAbility

    companion object : AbstractAbilityType<AirTornadoAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(AbilityExecutionType.SNEAK),
        id = "bending:air_tornado",
        name = "AirTornado"
    ) {
        override val default: Ability<AirTornadoAbility> = AirTornadoAbility(
            cooldown = 10000L,
            duration = 10000L,
            height = 20.0,
            pushFactor = 1.0,
            radius = 10.0,
            range = 25.0,
            speed = 1.0,
            particles = 4
        )

        override fun load(node: ConfigurationNode): AirTornadoAbility {
            TODO()
        }
    }

    private val numStreams: Int = (this.height * 0.3).toInt()

    private val random: Random = java.util.Random().asKotlinRandom()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[player] ?: return ErrorNoTarget
        val angles: MutableMap<Int, Int> = createAngleMap()

        val startTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (!player.getOrElse(Keys.IS_SNEAKING, false)) return Success
            if (player.eyeLocation.blockType.isLiquid()) return Success
            if (this.duration > 0 && startTime + this.duration <= System.currentTimeMillis()) return Success
        }
    }

    override fun cleanup(context: AbilityContext) {}

    private fun createAngleMap(): MutableMap<Int, Int> {
        val angles = HashMap<Int, Int>()
        var angle = 0
        var i = 0
        val di: Int = (this.height / this.numStreams).toInt()
        while (i <= this.height) {
            angles[i] = angle
            angle += 90
            if (angle == 360) angle = 0

            i += di
        }
        return angles
    }

    private fun rotateTornado(context: AbilityContext, player: Player, angles: MutableMap<Int, Int>) {
        var origin: Location<World> = player.getTargetLocation(this.radius)

        var currentHeight: Double by context.by(StandardContext.height)
        var currentRadius: Double by context.by(StandardContext.radius)

        val timeFactor: Double = currentHeight / this.height
        currentRadius = timeFactor * this.radius

        if (origin.blockType != BlockTypes.AIR && origin.blockType != BlockTypes.BARRIER) {
            origin = Location(origin.extent, origin.x, origin.y - 1.0 / 10.0 * currentHeight, origin.z)

            for (entity: Entity in origin.getNearbyEntities(currentHeight)) {
                if (ProtectionService.get().isProtected(player, origin)) continue

                val y: Double = entity.location.y
                if (y > origin.y && y < origin.y + currentHeight) {
                    val factor: Double = (y - origin.y) / currentHeight
                    val test: Location<World> = Location(origin.extent, origin.x, y, origin.z)

                    if (test.distance(entity.location) < currentRadius * factor) {
                        val angle: Double = Math.toRadians(100.0)

                        val x: Double = entity.location.x - origin.x
                        val z: Double = entity.location.z - origin.z
                        val magnitude: Double = sqrt(x * x + z * z)

                        var vx: Double = 0.0
                        var vy: Double = 0.7 * this.pushFactor
                        var vz: Double = 0.0

                        if (magnitude != 0.0) {
                            vx = (x * cos(angle) - z * sin(angle)) / magnitude
                            vz = (x * sin(angle) + z * cos(angle)) / magnitude
                        }

                        if (entity.uniqueId == player.uniqueId) {
                            val direction: Vector3d = player.headDirection.normalize()
                            vx = direction.x
                            vz = direction.z
                            val dy: Double = player.location.y - origin.y

                            if (dy >= currentHeight * 0.95) {
                                vy = 0.0
                            } else if (dy >= currentHeight * 0.85) {
                                vy = 6.0 * (0.95 - dy / currentHeight)
                            } else {
                                vy = 0.6
                            }
                        }

                        entity.velocity = Vector3d(vx, vy, vz).mul(timeFactor)
                        entity.offer(Keys.FALL_DISTANCE, 0F)
                    }
                }
            }

            for ((i, angle) in angles) {
                val rAngle: Double = Math.toRadians(angle.toDouble())
                val factor: Double = i / currentHeight
                val alpha = timeFactor * factor * currentRadius

                val x: Double = origin.x + alpha * cos(rAngle)
                val y: Double = origin.y + timeFactor * i
                val z: Double = origin.z + alpha * sin(rAngle)

                val effect: Location<World> = Location(origin.extent, x, y, z)

                if (!ProtectionService.get().isProtected(player, effect)) {
                    EffectService.get().createRandomParticle(Elements.Air, this.particles)
                    if (this.random.nextInt(20) == 0) {
                        // Play the sounds every now and then.
                        effect.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, effect.position, 0.5, 1.0)
                    }
                }
            }
        }

        currentHeight = if (currentHeight > height) height else currentHeight + 1
    }
}