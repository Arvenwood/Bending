package pw.dotdash.bending.plugin.command

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments.*
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors.GOLD
import org.spongepowered.api.text.format.TextColors.YELLOW
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.util.unwrap

object CommandBendingDisplay : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.display.base")
        .arguments(optional(onlyOne(player(Text.of("player")))))
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val target: Player = args.getOne<Player>(Text.of("player")).unwrap()
            ?: src as? Player
            ?: throw CommandException(Text.of("You must specify the player argument."))

        val bender: Bender = BenderService.getInstance().getOrCreateBender(target)

        val pagination: PaginationList = PaginationList.builder()
            .title(Text.of(YELLOW, "Abilities: ${target.name}"))
            .padding(Text.of(GOLD, "="))
            .contents(bender.hotbar.map { it.unwrap()?.type?.show() ?: Text.of("<none>") })
            .build()

        pagination.sendTo(src)

        return CommandResult.success()
    }
}