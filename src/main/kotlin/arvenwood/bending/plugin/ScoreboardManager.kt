package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.event.ExecuteAbilityEvent
import arvenwood.bending.api.event.SetCooldownEvent
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.util.selectedSlotIndex
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.scoreboard.Score
import org.spongepowered.api.scoreboard.Scoreboard
import org.spongepowered.api.scoreboard.critieria.Criteria
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots
import org.spongepowered.api.scoreboard.objective.Objective
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextStyles.STRIKETHROUGH

class ScoreboardManager {

    @Listener
    fun onSlotChange(event: ChangeInventoryEvent.Held, @Root player: Player) {
        val bender: Bender = BenderService.get()[player.uniqueId]
        player.scoreboard = newScoreboard(bender)
    }

    @Listener
    fun onSetCooldown(event: SetCooldownEvent) {
        val bender: Bender = BenderService.get()[event.targetEntity.uniqueId]
        event.targetEntity.scoreboard = newScoreboard(bender)
    }

    @Listener
    fun onExecuteAbility(event: ExecuteAbilityEvent) {
        event.targetEntity.scoreboard = newScoreboard(event.bender)
    }

    private fun newScoreboard(bender: Bender): Scoreboard {
        val objective: Objective = Objective.builder()
            .name("abilities")
            .criterion(Criteria.DUMMY)
            .displayName(Text.of("Abilities"))
            .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
            .build()

        val curIndex: Int = bender.player.selectedSlotIndex

        for ((index: Int, ability: Ability<*>?) in bender.equippedAbilities.withIndex()) {
            var display: Text = when {
                ability == null -> Text.of(" ".repeat(index + 1))
                bender.hasCooldown(ability.type) -> Text.of(ability.type.element.color, STRIKETHROUGH, ability.type.name)
                else -> Text.of(ability.type.element.color, ability.type.name)
            }

            if (index == curIndex) {
                display = Text.of("*", display)
            }

            val score: Score = objective.getOrCreateScore(display)
            score.score = index + 1
        }

        val scoreboard: Scoreboard = Scoreboard.builder()
            .objectives(listOf(objective))
            .build()

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR)
        return scoreboard
    }
}