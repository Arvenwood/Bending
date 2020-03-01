package pw.dotdash.bending.plugin

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
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.event.ExecuteAbilityEvent
import pw.dotdash.bending.api.event.SetCooldownEvent
import pw.dotdash.bending.api.util.selectedSlotIndex
import pw.dotdash.bending.api.util.unwrap
import java.util.*

class ScoreboardManager {

    @Listener
    fun onSlotChange(event: ChangeInventoryEvent.Held, @Root player: Player) {
        val bender: Bender = BenderService.getInstance().getOrCreateBender(player)
        player.scoreboard = newScoreboard(bender)
    }

    @Listener
    fun onSetCooldown(event: SetCooldownEvent) {
        val bender: Bender = BenderService.getInstance().getOrCreateBender(event.targetEntity)
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

        val curIndex: Int = bender.player.get().selectedSlotIndex

        for ((index: Int, abilityOpt: Optional<Ability>) in bender.hotbar.withIndex()) {
            val ability: Ability? = abilityOpt.unwrap()
            var display: Text = when {
                ability == null -> Text.of(" ".repeat(index + 1))
                bender.hasCooldown(ability.type) -> Text.of(ability.type.element.color, STRIKETHROUGH, ability.type.name)
                else -> Text.of(ability.type.element.color, ability.type.name)
            }

            if (index == curIndex) {
                display = Text.of("*", display)
            }

            val score: Score = objective.getOrCreateScore(display)
            score.score = 9 - index
        }

        val scoreboard: Scoreboard = Scoreboard.builder()
            .objectives(listOf(objective))
            .build()

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR)
        return scoreboard
    }
}