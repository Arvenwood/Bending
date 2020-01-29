package arvenwood.bending.api.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.enumSetOf
import arvenwood.bending.api.util.headDirection
import arvenwood.bending.api.util.isWater
import arvenwood.bending.api.util.spawnParticles
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class FireJetAbility(
    override val cooldown: Long,
    val duration: Long,
    val speed: Double,
    val showGliding: Boolean
) : Ability<FireJetAbility> {

    override val type: AbilityType<FireJetAbility> = FireJetAbility

    companion object : AbstractAbilityType<FireJetAbility>(
        element = Elements.Fire,
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:fire_jet",
        name = "FireJet"
    ) {
        override val default: Ability<FireJetAbility> = FireJetAbility(
            cooldown = 7000L,
            duration = 2000L,
            speed = 0.8,
            showGliding = false
        )

        override fun load(node: ConfigurationNode): FireJetAbility {
            TODO()
        }
    }

    private val random: Random = java.util.Random().asKotlinRandom()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[player] ?: return ErrorNoTarget

        val startTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }
            if (player.location.blockType.isWater()) {
                // Crashed into the water.
                return ErrorUnderWater
            }
            if (startTime + this.duration <= System.currentTimeMillis()) {
                // Duration limit exceeded.
                return Success
            }

            if (this.random.nextInt(2) == 0) {
                // Play fire bending sound, every now and then.
                player.world.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, player.position, 0.5, 1.0)
            }

            val flame = ParticleEffect.builder()
                .type(ParticleTypes.FLAME)
                .quantity(20)
                .offset(Vector3d(0.6, 0.6, 0.6))
                .build()
            val smoke = ParticleEffect.builder()
                .type(ParticleTypes.SMOKE)
                .quantity(10)
                .offset(Vector3d(0.6, 0.6, 0.6))
                .build()
            player.location.spawnParticles(flame)
            player.location.spawnParticles(smoke)

            // Launch them a bit.
            val timeFactor: Double = 1 - (System.currentTimeMillis() - startTime) / (2.0 * this.duration)
            player.velocity = player.headDirection.normalize().mul(this.speed * timeFactor)
            player.offer(Keys.FALL_DISTANCE, 0F)
        }
    }
}