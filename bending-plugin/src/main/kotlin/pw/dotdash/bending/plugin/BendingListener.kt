package pw.dotdash.bending.plugin

import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.cause.entity.damage.DamageTypes
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.DamageEntityEvent
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextStyles
import org.spongepowered.api.util.Direction.DOWN
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.*
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.util.unwrap
import pw.dotdash.bending.plugin.util.get

class BendingListener {

    @Listener
    fun onChangeSlot(event: ChangeInventoryEvent.Held, @Root player: Player) {
        displayAbility(player)
    }

    private fun displayAbility(player: Player) {
        val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
        val type: AbilityType = bender.selectedAbility.unwrap()?.type ?: return
        val onCooldown: Boolean = bender.hasCooldown(type)

        if (onCooldown) {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, TextStyles.STRIKETHROUGH, type.name))
        } else {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, type.name))
        }
    }

    @Listener
    fun onLeftClick(event: InteractBlockEvent.Primary.MainHand, @Root player: Player) {
        val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
        val ability: Ability = bender.selectedAbility.unwrap() ?: return
        bender.execute(ability, LEFT_CLICK)
    }

    @Listener
    fun onSneak(event: ChangeDataHolderEvent.ValueChange, @Root player: Player) {
        if (event.endResult[Keys.IS_SNEAKING] == true) {
            val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
            val ability: Ability = bender.selectedAbility.unwrap() ?: return
            bender.execute(ability, SNEAK)
        }

        val isSprinting: Boolean? = event.endResult[Keys.IS_SPRINTING]
        if (isSprinting != null) {
            val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
            val ability: Ability = bender.selectedAbility.unwrap() ?: return
            bender.execute(ability, if (isSprinting) SPRINT_ON else SPRINT_OFF)
        }
    }

    @Listener
    fun onJump(event: MoveEntityEvent, @Root player: Player) {
        if (event.fromTransform.position.y >= event.toTransform.position.y) {
            // Didn't go up.
            return
        }

        if (!event.fromTransform.location.getRelative(DOWN).hasBlock()) {
            // Block below them was air.
            return
        }

        val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
        val ability: Ability = bender.selectedAbility.unwrap() ?: return
        bender.execute(ability, JUMP)
    }

    @Listener
    fun onFall(event: DamageEntityEvent, @Root source: DamageSource, @Getter("getTargetEntity") player: Player) {
        if (source.type != DamageTypes.FALL) {
            // Ignore any damage that isn't from falling.
            return
        }
        if (event.willCauseDeath()) {
            // Don't activate abilities on death.
            return
        }

        val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
        val ability: Ability = bender.selectedAbility.unwrap() ?: return

        if (ability.type.id == "bending:air_agility") {
            event.isCancelled = true
            return
        }

        bender.execute(ability, FALL)
    }
}