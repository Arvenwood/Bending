package arvenwood.bending.plugin.command

import arvenwood.bending.api.Bender
import arvenwood.bending.api.service.BenderService
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

object CommandBendingDisplay : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.display.base")
        .arguments(optional(onlyOne(player(Text.of("player")))))
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val target: Player = args.getOne<Player>(Text.of("player")).orElse(null)
            ?: src as? Player
            ?: throw CommandException(Text.of("You must specify the player argument."))

        val bender: Bender = BenderService.get()[target.uniqueId]

        val pagination: PaginationList = PaginationList.builder()
            .title(Text.of(YELLOW, "Abilities: ${target.name}"))
            .padding(Text.of(GOLD, "="))
            .contents(bender.equippedAbilities.map { it?.type?.show() ?: Text.of("<none>") })
            .build()

        pagination.sendTo(src)

        return CommandResult.success()
    }
}