package arvenwood.bending.plugin.command

import arvenwood.bending.api.service.BenderService
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

object CommandBendingClear : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.clear")
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

        BenderService.get()[src.uniqueId].clearEquipped()
        src.sendMessage(Text.of(TextColors.GREEN, "Cleared all equipped abilities."))

        return CommandResult.success()
    }
}