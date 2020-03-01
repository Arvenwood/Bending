package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.util.headDirection
import pw.dotdash.bending.api.util.rotateAroundAxisX
import pw.dotdash.bending.api.util.rotateAroundAxisY
import pw.dotdash.bending.api.util.spawnParticles
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin

data class AirJumpAbility(
    override val cooldownMilli: Long,
    val multiplier: Double,
    val particleRadius: Double = 1.0
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_JUMP) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        multiplier = node.getNode("multiplier").double
    )

    private val particleEffect: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.EXPLOSION)
        .quantity(2)
        .offset(Vector3d.ZERO)
        .build()

    private val angularVelocity: Double = Math.PI / 16

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)
        val direction: Vector3d = player.headDirection

        var step = 0

        for (x: Int in 0 until 10) {
            if (step > 180) {
                step = 0
            }

            val angle: Double = step * angularVelocity
            val radius: Double = step * 0.006
            val length: Double = step * 0.05

            val vec: Vector3d = Vector3d(cos(angle) * radius, length, sin(angle) * radius)
                .rotateAroundAxisX(Math.toRadians(direction.x + 90))
                .rotateAroundAxisY(Math.toRadians(-direction.y))

            player.location.add(vec)
                .spawnParticles(EffectService.getInstance().createRandomParticle(Elements.AIR, 4))

            step++
        }

        player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 1.0, 0.5)

        player.velocity = player.headDirection.mul(multiplier)
    }
}