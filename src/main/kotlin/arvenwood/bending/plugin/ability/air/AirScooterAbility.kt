package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.util.EpochTime
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Direction.UP
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin

data class AirScooterAbility(
    override val cooldown: Long,
    val duration: Long,
    val interval: Long,
    val maxGroundHeight: Double,
    val radius: Double,
    val speed: Double
) : Ability<AirScooterAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        interval = node.getNode("interval").long,
        maxGroundHeight = node.getNode("maxGroundHeight").double,
        radius = node.getNode("radius").double,
        speed = node.getNode("speed").double
    )

    override val type: AbilityType<AirScooterAbility> = AbilityTypes.AIR_SCOOTER

    private val minVelocitySquared: Double = (this.speed * 0.3) * (this.speed * 0.3)

    private val particleEffect: ParticleEffect = EffectService.get().createParticle(Elements.AIR, 1, Vector3d.ZERO)

    // offsets[phiIndex][thetaIndex] = offset
    private val offsets: Array<Array<Vector3d>> = this.calculateOffsets()

    override fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(StandardContext.player)
        // Cancel all other air scooters.
        BenderService.get()[player.uniqueId].cancel(this.type)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        player.canFly = true
        player.isFlying = true

        player.isSprinting = false
        player.isSneaking = false

        var phiIndex = 0
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.isSneaking) return Success
            if (this.duration > 0 && startTime.elapsedNow() >= this.duration) return Success

            if (startTime.elapsedNow() >= this.interval) {
                // Only display particles and check velocity every now and then.
                if (player.velocity.lengthSquared() < this.minVelocitySquared) {
                    // Too slow!
                    return Success
                }

                for (offset: Vector3d in this.offsets[phiIndex]) {
                    player.location.add(offset).spawnParticles(this.particleEffect)
                    player.location.sub(offset).spawnParticles(this.particleEffect)
                }

                phiIndex++
                if (phiIndex == 5) {
                    phiIndex = 0
                }
            }

            val floor: Location<World> = this.getFloor(player.eyeLocation) ?: return Success

            var velocity: Vector3d = player.headDirection.normalize().mul(this.speed)

            val distance: Double = player.location.y - floor.y
            velocity = when {
                distance > 2.75 -> velocity.withY(-0.25)
                distance < 2.0 -> velocity.withY(0.25)
                else -> velocity.withY(0.0)
            }

            val forward: Location<World> = floor.add(velocity.withY(0.0).mul(1.2))
            if (!(forward.blockType.isSolid() || forward.blockType.isWater())) {
                velocity = velocity.add(0.0, -0.1, 0.0)
            } else if (forward.getRelative(UP).blockType.isSolid() || forward.getRelative(UP).blockType.isWater()) {
                velocity = velocity.add(0.0, 0.7, 0.0)
            }

            if (!player.location.add(0.0, 2.0, 0.0).blockType.isWater()) {
                player.location = player.location.withY(floor.y + 1.5)
            } else {
                return@abilityLoopUnsafe
            }

            player.isSprinting = false
            player.velocity = velocity

            if (Constants.RANDOM.nextInt(4) == 0) {
                // Play the sounds every now and then.
                player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 0.5, 1.0)
            }
        }
    }

    override fun cleanup(context: AbilityContext) {
        val player: Player = context.player

        player.canFly = false
        player.isFlying = false
    }

    private fun getFloor(origin: Location<World>): Location<World>? {
        for (i: Int in 0..this.maxGroundHeight.toInt()) {
            val below: Location<World> = origin.add(0, -i, 0)

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