package pw.dotdash.bending.classic.ability.air

import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes.JUMP_BOOST
import org.spongepowered.api.effect.potion.PotionEffectTypes.SPEED
import org.spongepowered.api.entity.living.player.Player
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.BENDER
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.SPRINT_OFF
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.SPRINT_ON
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes

data class AirAgilityAbility(
    override val cooldownMilli: Long,
    val jumpPower: Int,
    val speedPower: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_AGILITY) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        jumpPower = node.getNode("jumpPower").int,
        speedPower = node.getNode("speedPower").int
    )

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

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        if (executionType !== SPRINT_ON) {
            return
        }

        val player: Player = context.require(PLAYER)
        val bender: Bender = context.require(BENDER)

        player.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect>? ->
            val newEffects = effects?.toMutableList() ?: arrayListOf()
            newEffects += effectJump
            newEffects += effectSpeed
            newEffects
        }

        bender.waitForExecution(type, SPRINT_OFF)

        player.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect>? ->
            effects.orEmpty().filterNot {
                (it.type == JUMP_BOOST && it.amplifier == jumpPower) || (it.type == SPEED && it.amplifier == speedPower)
            }
        }
    }
}