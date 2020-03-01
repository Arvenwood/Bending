package pw.dotdash.bending.plugin.command

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.util.unwrap
import java.util.*

object CommandBendingCopy : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.copy.base")
        .arguments(GenericArguments.player(Text.of("player")))
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

        val target: Player = args.requireOne("player")

        val srcBender: Bender = BenderService.getInstance().getOrCreateBender(src)
        val targetBender: Bender = BenderService.getInstance().getOrCreateBender(target)

        srcBender.clearEquippedAbilities()
        for ((index: Int, ability: Optional<Ability>) in targetBender.hotbar.withIndex()) {
            srcBender.setEquippedAbility(index, ability.unwrap())
        }

        return CommandResult.success()
    }
}