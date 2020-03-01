package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
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
import pw.dotdash.bending.api.util.headDirection
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes

data class AirJumpAbility(
    override val cooldownMilli: Long,
    val multiplier: Double,
    val particleRadius: Double = 1.0
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_JUMP) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        multiplier = node.getNode("multiplier").double
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val particleEffect: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.EXPLOSION)
        .quantity(2)
        .offset(Vector3d.ZERO)
        .build()

    private val angularVelocity: Double = Math.PI / 16

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 1.0, 0.5)
        player.velocity = player.headDirection.mul(multiplier)
    }
}