package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.headDirection
import arvenwood.bending.api.util.rotateAroundAxisX
import arvenwood.bending.api.util.rotateAroundAxisY
import arvenwood.bending.api.util.spawnParticles
import arvenwood.bending.plugin.ability.AbilityTypes
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import kotlin.math.cos
import kotlin.math.sin

data class AirJumpAbility(
    override val cooldown: Long,
    val multiplier: Double,
    val particleRadius: Double = 1.0
) : Ability<AirJumpAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        multiplier = node.getNode("multiplier").double
    )

    override val type: AbilityType<AirJumpAbility> = AbilityTypes.AIR_JUMP

    private val particleEffect: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.EXPLOSION)
        .quantity(2)
        .offset(Vector3d.ZERO)
        .build()

    private val angularVelocity: Double = Math.PI / 16

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[StandardContext.player] ?: return ErrorNoTarget
        val direction: Vector3d = player.headDirection

        var step = 0

        for (x: Int in 0 until 10) {
            if (step > 180) {
                step = 0
            }

            val angle: Double = step * this.angularVelocity
            val radius: Double = step * 0.006
            val length: Double = step * 0.05

            val vec: Vector3d = Vector3d(cos(angle) * radius, length, sin(angle) * radius)
                .rotateAroundAxisX(Math.toRadians(direction.x + 90))
                .rotateAroundAxisY(Math.toRadians(-direction.y))

            player.location.add(vec)
                .spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, 4))

            step++
        }

        player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 1.0, 0.5)

        player.velocity = player.headDirection.mul(this.multiplier)
        return Success
    }
}