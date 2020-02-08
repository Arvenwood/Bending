package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.enumSetOf
import arvenwood.bending.api.util.isSprinting
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.effect.potion.PotionEffectTypes.JUMP_BOOST
import org.spongepowered.api.effect.potion.PotionEffectTypes.SPEED
import org.spongepowered.api.entity.living.player.Player

data class AirAgilityAbility(
    override val cooldown: Long,
    val jumpPower: Int,
    val speedPower: Int
) : Ability<AirAgilityAbility> {

    override val type: AbilityType<AirAgilityAbility> = AirAgilityAbility

    companion object : AbstractAbilityType<AirAgilityAbility>(
        element = Elements.Air,
        executionTypes = enumSetOf(SPRINT_ON, SPRINT_OFF),
        id = "bending:air_agility",
        name = "AirAgility"
    ) {
        override fun load(node: ConfigurationNode): AirAgilityAbility = AirAgilityAbility(
            cooldown = node.getNode("cooldown").long,
            jumpPower = node.getNode("jumpPower").int,
            speedPower = node.getNode("speedPower").int
        )
    }

    private val effectJump: PotionEffect =
        PotionEffect.builder()
            .potionType(JUMP_BOOST)
            .amplifier(this.jumpPower)
            .duration(Int.MAX_VALUE)
            .particles(false)
            .build()

    private val effectSpeed: PotionEffect =
        PotionEffect.builder()
            .potionType(SPEED)
            .amplifier(this.speedPower)
            .duration(Int.MAX_VALUE)
            .particles(false)
            .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        if (executionType !== SPRINT_ON) {
            return Success // TODO: better result type
        }

        val player: Player = context.require(StandardContext.player)
        val bender: Bender = context.require(StandardContext.bender)

        player.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect>? ->
            val effects = effects?.toMutableList() ?: arrayListOf()
            effects += this.effectJump
            effects += this.effectSpeed
            effects
        }

        bender.awaitExecution(AirAgilityAbility, SPRINT_OFF)

        player.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect>? ->
            effects.orEmpty().filterNot {
                (it.type == JUMP_BOOST && it.amplifier == this.jumpPower) || (it.type == SPEED && it.amplifier == this.speedPower)
            }
        }

        return Success
    }
}