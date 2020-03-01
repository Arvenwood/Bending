package pw.dotdash.bending.plugin.command

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandPermissionException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments.catalogedElement
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextColors
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.ability.AbilityConfig
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.util.unwrap

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

        val type: AbilityType = args.requireOne("ability")
        val config: AbilityConfig = args.requireOne("config")

        if (!src.hasPermission("bending.config.${config.name}")) {
            throw CommandPermissionException(Text.of("You do not have permission to use that config!"))
        }

        val ability: Ability = config.load(type).unwrap() ?: throw CommandException(Text.of("That config doesn't have ${type.id}"))

        BenderService.getInstance().getOrCreateBender(src).setSelectedAbility(ability)
        src.sendMessage(
            ChatTypes.ACTION_BAR,
            Text.of(
                "Selected Ability (config ",
                TextColors.LIGHT_PURPLE, config.name,
                TextColors.RESET, "): ", type.element.color, type.name
            )
        )

        return CommandResult.success()
    }
}