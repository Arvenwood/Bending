package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.enumSetOf
import arvenwood.bending.api.util.isSprinting
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.entity.living.player.Player

data class AirAgilityAbility(
    override val cooldown: Long,
    val duration: Long,
    val jumpPower: Int,
    val speedPower: Int
) : Ability<AirAgilityAbility> {

    override val type: AbilityType<AirAgilityAbility> = AirAgilityAbility

    companion object : AbstractAbilityType<AirAgilityAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(LEFT_CLICK),
        id = "bending:air_ability",
        name = "AirAgility"
    ) {
        override fun load(node: ConfigurationNode): AirAgilityAbility = AirAgilityAbility(
            cooldown = node.getNode("cooldown").long,
            duration = node.getNode("duration").long,
            jumpPower = node.getNode("jumpPower").int,
            speedPower = node.getNode("speedPower").int
        )
    }

    private val effectJump: PotionEffect =
        PotionEffect.builder()
            .potionType(PotionEffectTypes.JUMP_BOOST)
            .amplifier(this.jumpPower)
            .duration(5)
            .particles(false)
            .build()

    private val effectSpeed: PotionEffect =
        PotionEffect.builder()
            .potionType(PotionEffectTypes.SPEED)
            .amplifier(this.speedPower)
            .duration(5)
            .particles(false)
            .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[StandardContext.player] ?: return ErrorNoTarget

        abilityLoopUnsafe {
            if (player.isRemoved) return Success
            if (!player.isSprinting) return Success

            player.transform(Keys.POTION_EFFECTS) { effects: MutableList<PotionEffect> ->
                if (effects.none { it.type == PotionEffectTypes.JUMP_BOOST && it.amplifier >= this.jumpPower }) {
                    effects += effectJump
                }
                if (effects.none { it.type == PotionEffectTypes.SPEED && it.amplifier >= this.speedPower }) {
                    effects += effectSpeed
                }

                effects
            }
        }
    }
}