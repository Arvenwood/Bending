package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Direction.UP
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class AirScooterAbility(
    override val cooldown: Long,
    val duration: Long,
    val interval: Long,
    val maxGroundHeight: Double,
    val radius: Double,
    val speed: Double
) : Ability<AirScooterAbility> {

    override val type: AbilityType<AirScooterAbility> = AirScooterAbility

    companion object : AbstractAbilityType<AirScooterAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:air_scooter",
        name = "AirScooter"
    ) {
        override val default: Ability<AirScooterAbility>
            get() = TODO("not implemented")

        override fun load(node: ConfigurationNode): AirScooterAbility = AirScooterAbility(
            cooldown = node.getNode("cooldown").long,
            duration = node.getNode("duration").long,
            interval = node.getNode("interval").long,
            maxGroundHeight = node.getNode("maxGroundHeight").double,
            radius = node.getNode("radius").double,
            speed = node.getNode("speed").double
        )

        private const val PHI_INCREMENT: Double = Math.PI * 2 / 5
        private const val TWO_PI: Double = Math.PI * 2
        private const val TENTH_PI: Double = Math.PI / 10
        private const val PARTICLE_RADIUS: Double = 0.6
    }

    private val minVelocitySquared: Double = (this.speed * 0.3) * (this.speed * 0.3)

    private val particleEffect: ParticleEffect get() =
        EffectService.get().createParticle(Elements.Air, 1, Vector3d.ZERO)

    private val random: Random = java.util.Random().asKotlinRandom()

    override fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(StandardContext.player)
        // Cancel all other air scooters.
        BenderService.get()[player.uniqueId].cancel(AirScooterAbility)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        val startTime: Long = System.currentTimeMillis()
        var phi = 0.0
        abilityLoopUnsafe {
            if (player.isSneaking) return Success
            if (this.duration > 0 && startTime + this.duration <= System.currentTimeMillis()) return Success

            if (startTime + this.interval <= System.currentTimeMillis()) {
                // Only display particles and check velocity every now and then.

                if (player.velocity.lengthSquared() < this.minVelocitySquared) {
                    // Too slow!
                    return Success
                }

                phi = this.displayScooter(phi, context)
            }

            val origin: Location<World> = player.eyeLocation
            val floor: Location<World> = getFloor(origin) ?: return Success

            var velocity: Vector3d = player.headDirection.normalize().mul(this.speed)

            val distance: Double = player.location.y - floor.y
            velocity = when {
                distance > 2.75 -> velocity.withY(-0.25)
                distance < 2.0 -> velocity.withY(0.25)
                else -> velocity.withY(0.0)
            }

            val forward: Location<World> = floor.add(velocity.mul(1.2))
            if (!(forward.blockType.isSolid() || forward.blockType.isWater())) {
                velocity = velocity.add(0.0, -0.1, 0.0)
            } else if (forward.getRelative(UP).blockType.isSolid() || forward.getRelative(UP).blockType.isWater()) {
                velocity = velocity.add(0.0, 0.7, 0.0)
            }

            var location: Location<World> = player.location
            if (!location.add(0.0, 2.0, 0.0).blockType.isWater()) {
                location = location.withY(floor.y + 1.5)
            } else {
                return@abilityLoopUnsafe
            }

            player.isSprinting = false
            player.removePotionEffectByType(PotionEffectTypes.SPEED)

            player.velocity = velocity

            if (this.random.nextInt(4) == 0) {
                // Play the sounds every now and then.
                location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, location.position, 0.5, 1.0)
            }
        }
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

    private fun displayScooter(phi: Double, context: AbilityContext): Double {
        val origin: Location<World> = context.require(StandardContext.origin)

        val newPhi: Double = phi + PHI_INCREMENT
        val sinPhi: Double = sin(newPhi)
        val cosPhi: Double = cos(newPhi)

        var theta = 0.0
        while (theta <= TWO_PI) {
            val x: Double = PARTICLE_RADIUS * cos(theta) * sinPhi
            val y: Double = PARTICLE_RADIUS * cosPhi
            val z: Double = PARTICLE_RADIUS * sin(theta) * sinPhi

            origin.add(x, y, z).spawnParticles(this.particleEffect)
            origin.sub(x, y, z).spawnParticles(this.particleEffect)

            theta += TENTH_PI
        }

        return newPhi
    }
}