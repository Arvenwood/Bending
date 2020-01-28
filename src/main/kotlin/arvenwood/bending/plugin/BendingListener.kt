package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.CooldownService
import arvenwood.bending.api.util.get
import arvenwood.bending.api.util.index
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextStyles

class BendingListener {

    @Listener
    fun onJoin(event: ClientConnectionEvent.Join) {
        displayAbility(event.targetEntity)
    }

    @Listener
    fun onChangeSlot(event: ChangeInventoryEvent.Held, @First player: Player) {
        val oldSlot: Int = event.originalSlot.index
        val newSlot: Int = event.finalSlot.index

        if (oldSlot == newSlot) return

        displayAbility(player)
    }

    private fun displayAbility(player: Player) {
        val type: AbilityType<Ability<*>> = BenderService.get()[player.uniqueId].selectedAbility?.type ?: return
        val onCooldown: Boolean = CooldownService.get().hasCooldown(player, type)

        if (onCooldown) {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, TextStyles.STRIKETHROUGH, type.name))
        } else {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, type.name))
        }
    }

    @Listener
    fun onLeftClick(event: InteractBlockEvent.Primary.MainHand, @First player: Player) {
        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.LEFT_CLICK)
    }

    @Listener
    fun onRightClick(event: InteractEntityEvent.Secondary.MainHand, @First player: Player) {
        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.RIGHT_CLICK)
    }

    @Listener
    fun onSneak(event: ChangeDataHolderEvent.ValueChange, @First player: Player) {
        if (event.endResult[Keys.IS_SNEAKING] == true) {
            val bender: Bender = BenderService.get()[player.uniqueId]
            val ability: Ability<*> = bender.selectedAbility ?: return
            bender.execute(ability, AbilityExecutionType.SNEAK)
        }
    }
}