package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.Sneak
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.headDirection
import arvenwood.bending.api.util.spawnParticles
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player

data class AirJumpAbility(
    override val cooldown: Long,
    val multiplier: Double
) : Ability<AirJumpAbility> {

    override val type: AbilityType<AirJumpAbility> = AirJumpAbility

    companion object : AbstractAbilityType<AirJumpAbility>(
        element = Elements.Air,
        executionTypes = setOf(Sneak::class),
        id = "bending:air_jump",
        name = "AirJump"
    ) {
        override val default: Ability<AirJumpAbility> = AirJumpAbility(
            cooldown = 6000L,
            multiplier = 3.0
        )

        override fun load(node: ConfigurationNode): AirJumpAbility = AirJumpAbility(
            cooldown = node.getNode("cooldown").long,
            multiplier = node.getNode("multiplier").double
        )
    }

    private val particleEffect: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.EXPLOSION)
        .quantity(2)
        .offset(Vector3d.ZERO)
        .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[StandardContext.player] ?: return ErrorNoTarget

        player.velocity = player.headDirection.mul(this.multiplier)
        player.location.spawnParticles(this.particleEffect)
        player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 1.0, 0.5)
        return Success
    }
}