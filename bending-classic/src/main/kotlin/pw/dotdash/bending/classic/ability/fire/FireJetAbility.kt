package pw.dotdash.bending.classic.ability.fire

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.util.EpochTime
import pw.dotdash.bending.api.util.headDirection
import pw.dotdash.bending.api.util.isWater
import pw.dotdash.bending.api.util.spawnParticles
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.random.Random

data class FireJetAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val speed: Double,
    val showGliding: Boolean
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.FIRE_JET) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        speed = node.getNode("speed").double,
        showGliding = node.getNode("showGliding").boolean
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val particleFlame: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(20)
        .offset(Vector3d(0.6, 0.6, 0.6))
        .build()
    private val particleSmoke: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(10)
        .offset(Vector3d(0.6, 0.6, 0.6))
        .build()

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return
            }
            if (player.location.blockType.isWater()) {
                // Crashed into the water.
                return
            }
            if (startTime.elapsedNow() >= duration) {
                // Duration limit exceeded.
                return
            }

            if (Random.nextInt(2) == 0) {
                // Play fire bending sound, every now and then.
                player.world.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, player.position, 0.5, 1.0)
            }

            player.location.spawnParticles(particleFlame)
            player.location.spawnParticles(particleSmoke)

            // Launch them a bit.
            val timeFactor: Double = 1 - (startTime.elapsedNow()) / (2.0 * duration)
            player.velocity = player.headDirection.normalize().mul(speed * timeFactor)
            player.offer(Keys.FALL_DISTANCE, 0F)
        }
    }
}