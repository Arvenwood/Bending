package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.SNEAK
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin

data class AirJumpAbility(
    override val cooldown: Long,
    val multiplier: Double,
    val particleRadius: Double = 1.0
) : Ability<AirJumpAbility> {

    override val type: AbilityType<AirJumpAbility> = AirJumpAbility

    companion object : AbstractAbilityType<AirJumpAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(SNEAK),
        id = "bending:air_jump",
        name = "AirJump"
    ) {
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

        forInclusive(from = 0.0, to = 50.0, step = 0.05) { y: Double ->
            val x: Double = this.particleRadius * cos(y)
            val z: Double = this.particleRadius * sin(y)
            val location: Location<World> = player.location.add(Vector3d(x, y, z).rotateAroundAxis(player.headRotation.normalize()))
            location.spawnParticles(this.particleEffect)
        }

        player.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.location.position, 1.0, 0.5)

        player.velocity = player.headDirection.mul(this.multiplier)
        return Success
    }
}