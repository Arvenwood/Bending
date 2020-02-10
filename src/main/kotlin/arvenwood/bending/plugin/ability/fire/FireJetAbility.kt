package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.util.headDirection
import arvenwood.bending.api.util.isWater
import arvenwood.bending.api.util.spawnParticles
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.util.EpochTime
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player

data class FireJetAbility(
    override val cooldown: Long,
    val duration: Long,
    val speed: Double,
    val showGliding: Boolean
) : Ability<FireJetAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        speed = node.getNode("speed").double,
        showGliding = node.getNode("showGliding").boolean
    )

    override val type: AbilityType<FireJetAbility> = AbilityTypes.FIRE_JET

    private val particleFlame = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(20)
        .offset(Vector3d(0.6, 0.6, 0.6))
        .build()
    private val particleSmoke = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(10)
        .offset(Vector3d(0.6, 0.6, 0.6))
        .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.player

        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }
            if (player.location.blockType.isWater()) {
                // Crashed into the water.
                return ErrorUnderWater
            }
            if (startTime.elapsedNow() >= this.duration) {
                // Duration limit exceeded.
                return Success
            }

            if (Constants.RANDOM.nextInt(2) == 0) {
                // Play fire bending sound, every now and then.
                player.world.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, player.position, 0.5, 1.0)
            }

            player.location.spawnParticles(this.particleFlame)
            player.location.spawnParticles(this.particleSmoke)

            // Launch them a bit.
            val timeFactor: Double = 1 - (startTime.elapsedNow()) / (2.0 * this.duration)
            player.velocity = player.headDirection.normalize().mul(this.speed * timeFactor)
            player.offer(Keys.FALL_DISTANCE, 0F)
        }
    }
}