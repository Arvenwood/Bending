package arvenwood.bending.plugin.command

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.service.BenderService
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandPermissionException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.args.GenericArguments.catalogedElement
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextColors

object CommandBendingBind : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.bind.base")
        .arguments(
            catalogedElement(Text.of("ability"), AbilityType::class.java),
            abilityConfig(Text.of("config"), Text.of("ability"))
        )
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

        val config: AbilityConfig = args.requireOne("config")

        if (!src.hasPermission("bending.user.bind.config.${config.name}")) {
            throw CommandPermissionException(Text.of("You do not have permission to use that config!"))
        }

        BenderService.get()[src.uniqueId].selectedAbility = config.ability
        src.sendMessage(
            ChatTypes.ACTION_BAR,
            Text.of(
                "Selected Ability (config ",
                TextColors.LIGHT_PURPLE, config.name,
                TextColors.RESET, "): ", config.type.element.color, config.type.name
            )
        )

        return CommandResult.success()
    }
}