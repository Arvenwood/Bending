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
import org.spongepowered.api.event.cause.entity.damage.DamageTypes
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.DamageEntityEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextStyles
import org.spongepowered.api.util.Direction.DOWN

class BendingListener {

    @Listener
    fun onJoin(event: ClientConnectionEvent.Join) {
        displayAbility(event.targetEntity)
    }

    @Listener
    fun onChangeSlot(event: ChangeInventoryEvent.Held, @Root player: Player) {
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
    fun onLeftClick(event: InteractBlockEvent.Primary.MainHand, @Root player: Player) {
        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.LEFT_CLICK)
    }

    @Listener
    fun onRightClick(event: InteractEntityEvent.Secondary.MainHand, @Root player: Player) {
        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.RIGHT_CLICK)
    }

    @Listener
    fun onSneak(event: ChangeDataHolderEvent.ValueChange, @Root player: Player) {
        if (event.endResult[Keys.IS_SNEAKING] == true) {
            val bender: Bender = BenderService.get()[player.uniqueId]
            val ability: Ability<*> = bender.selectedAbility ?: return
            bender.execute(ability, AbilityExecutionType.SNEAK)
        }
        if (event.endResult[Keys.IS_SPRINTING] == true) {
            val bender: Bender = BenderService.get()[player.uniqueId]
            val ability: Ability<*> = bender.selectedAbility ?: return
            bender.execute(ability, AbilityExecutionType.SPRINT)
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

        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.JUMP)
    }

    @Listener
    fun onFall(event: DamageEntityEvent, @Root source: DamageSource, @Getter("getTargetEntity") player: Player) {
        if (event.willCauseDeath()) {
            // Don't activate abilities on death.
            return
        }

        if (source.type != DamageTypes.FALL) {
            // Ignore any damage that isn't from falling.
            return
        }

        player.sendMessage(Text.of("Fell ${player.getOrElse(Keys.FALL_DISTANCE, -1.0F)} blocks"))

        val bender: Bender = BenderService.get()[player.uniqueId]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.FALL)
    }
}