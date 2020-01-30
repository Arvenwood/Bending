package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.living.player.Player
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

    override val type: AbilityType<AirScooterAbility> = AirScooterAbility

    companion object : AbstractAbilityType<AirScooterAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:air_scooter",
        name = "AirScooter"
    ) {
        override val default: Ability<AirScooterAbility>
            get() = TODO("not implemented")

        override fun load(node: ConfigurationNode): AirScooterAbility {
            TODO("not implemented")
        }

        private const val PHI_INCREMENT: Double = Math.PI * 2 / 5
        private const val TWO_PI: Double = Math.PI * 2
        private const val TENTH_PI: Double = Math.PI / 10
        private const val PARTICLE_RADIUS: Double = 0.6
    }

    private val minVelocitySquared: Double = (this.speed * 0.3) * (this.speed * 0.3)

    private val particleEffect: ParticleEffect get() =
        EffectService.get().createParticle(Elements.Air, 1, Vector3d.ZERO)

    override fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(StandardContext.player)
        // Cancel all other air scooters.
        BenderService.get()[player.uniqueId].cancel(AirScooterAbility)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        val startTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (player.getOrElse(Keys.IS_SNEAKING, false)) return Success
            if (this.duration > 0 && startTime + this.duration <= System.currentTimeMillis()) return Success

            val origin: Location<World> = player.eyeLocation
            val floor: Location<World>? = getFloor(origin) ?: return Success

            val newVelocity: Vector3d = player.headDirection.normalize().mul(this.speed)

            if (startTime + this.interval <= System.currentTimeMillis()) {
                // Only display particles and check velocity every now and then.

                if (player.velocity.lengthSquared() < this.minVelocitySquared) {
                    // Too slow!
                    return Success
                }

                this.displayScooter(context)
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

    private fun displayScooter(context: AbilityContext) {
        val origin: Location<World> = context.require(StandardContext.origin)

        val phi: Double = context.require(phi) + PHI_INCREMENT
        context[AirScooterAbility.phi] = phi

        val sinPhi = sin(phi)
        val cosPhi = cos(phi)

        var theta = 0.0
        while (theta <= TWO_PI) {
            val x: Double = PARTICLE_RADIUS * cos(theta) * sinPhi
            val y: Double = PARTICLE_RADIUS * cosPhi
            val z: Double = PARTICLE_RADIUS * sin(theta) * sinPhi

            origin.add(x, y, z).spawnParticles(this.particleEffect)
            origin.sub(x, y, z).spawnParticles(this.particleEffect)

            theta += TENTH_PI
        }
    }

    object phi : AbilityContext.Key<Double>(id = "bending:phi", name = "Phi Context")
}