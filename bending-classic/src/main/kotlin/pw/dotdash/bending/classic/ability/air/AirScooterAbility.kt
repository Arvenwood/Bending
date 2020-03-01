package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Direction.UP
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.BENDER
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AirScooterAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val interval: Long,
    val maxGroundHeight: Double,
    val radius: Double,
    val speed: Double
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_SCOOTER) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        interval = node.getNode("interval").long,
        maxGroundHeight = node.getNode("maxGroundHeight").double,
        radius = node.getNode("radius").double,
        speed = node.getNode("speed").double
    )

    private val minVelocitySquared: Double = (this.speed * 0.3) * (this.speed * 0.3)

    private val particleEffect: ParticleEffect = EffectService.getInstance().createParticle(Elements.AIR, 1, Vector3d.ZERO)

    // offsets[phiIndex][thetaIndex] = offset
    private val offsets: Array<Array<Vector3d>> = this.calculateOffsets()

    override fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {
        val bender: Bender = context.require(BENDER)
        // Cancel all other air scooters.
        bender.cancel(this.type)
    }

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        player.canFly = true
        player.isFlying = true

        player.isSprinting = false
        player.isSneaking = false

        var phiIndex = 0
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.isSneaking) {
                return
            }
            if (duration > 0 && startTime.elapsedNow() >= duration) {
                return
            }

            if (startTime.elapsedNow() >= interval) {
                // Only display particles and check velocity every now and then.
                if (player.velocity.lengthSquared() < minVelocitySquared) {
                    // Too slow!
                    return
                }

                for (offset: Vector3d in offsets[phiIndex]) {
                    player.location.add(offset).spawnParticles(particleEffect)
                    player.location.sub(offset).spawnParticles(particleEffect)
                }

                phiIndex++
                if (phiIndex == 5) {
                    phiIndex = 0
                }
            }

            val floor: Location<World> = getFloor(player.eyeLocation) ?: return

            var velocity: Vector3d = player.headDirection.normalize().mul(speed)

            val distance: Double = player.location.y - floor.y
            velocity = when {
                distance > 2.75 -> velocity.setY(-0.25)
                distance < 2.0 -> velocity.setY(0.25)
                else -> velocity.setY(0.0)
            }

            val forward: Location<World> = floor.add(velocity.setY(0.0).mul(1.2))
            if (!(forward.blockType.isSolid() || forward.blockType.isWater())) {
                velocity = velocity.add(0.0, -0.1, 0.0)
            } else if (forward.getRelative(UP).blockType.isSolid() || forward.getRelative(UP).blockType.isWater()) {
                velocity = velocity.add(0.0, 0.7, 0.0)
            }

            if (!player.location.add(0.0, 2.0, 0.0).blockType.isWater()) {
                player.location = player.location.setY(floor.y + 1.5)
            } else {
                return@abilityLoopUnsafe
            }

            player.isSprinting = false
            player.velocity = velocity

            if (Random.nextInt(4) == 0) {
                // Play the sounds every now and then.
                player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 0.5, 1.0)
            }
        }
    }

    override fun cleanup(context: AbilityContext) {
        val player: Player = context.require(PLAYER)

        player.canFly = false
        player.isFlying = false
    }

    private fun getFloor(origin: Location<World>): Location<World>? {
        for (i: Int in 0..this.maxGroundHeight.toInt()) {
            val below: Location<World> = origin.add(0.0, (-i).toDouble(), 0.0)

            if (below.blockType.isSolid() || below.blockType.isWater()) {
                return below
            }
        }
        return null
    }

    // offsets[phiIndex][thetaIndex] = offset
    private fun calculateOffsets(): Array<Array<Vector3d>> =
        Array(size = 5) { index: Int ->
            calculateOffsets(TWO_FIFTHS_PI * index)
        }

    // offsets[thetaIndex] = offset
    private fun calculateOffsets(phi: Double): Array<Vector3d> {
        val sinPhi: Double = sin(phi)
        val cosPhi: Double = cos(phi)

        return Array(size = 20) { index: Int ->
            val theta: Double = TENTH_PI * index

            Vector3d(
                PARTICLE_RADIUS * cos(theta) * sinPhi,
                PARTICLE_RADIUS * cosPhi,
                PARTICLE_RADIUS * sin(theta) * sinPhi
            )
        }
    }

    companion object {
        private const val PARTICLE_RADIUS: Double = 0.6

        private const val TWO_FIFTHS_PI: Double = Math.PI * 2 / 5
        private const val TENTH_PI: Double = Math.PI / 10
    }
}