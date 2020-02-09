package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionTypes.SPRINT_OFF
import arvenwood.bending.api.ability.AbilityExecutionTypes.SPRINT_ON
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.plugin.ability.AbilityTypes
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes.JUMP_BOOST
import org.spongepowered.api.effect.potion.PotionEffectTypes.SPEED
import org.spongepowered.api.entity.living.player.Player

data class AirAgilityAbility(
    override val cooldown: Long,
    val jumpPower: Int,
    val speedPower: Int
) : Ability<AirAgilityAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        jumpPower = node.getNode("jumpPower").int,
        speedPower = node.getNode("speedPower").int
    )

    override val type: AbilityType<AirAgilityAbility> = AbilityTypes.AIR_AGILITY

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

        bender.awaitExecution(this.type, SPRINT_OFF)

        player.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect>? ->
            effects.orEmpty().filterNot {
                (it.type == JUMP_BOOST && it.amplifier == this.jumpPower) || (it.type == SPEED && it.amplifier == this.speedPower)
            }
        }

        return Success
    }
}