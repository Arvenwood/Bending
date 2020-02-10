package arvenwood.bending.plugin.command

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.service.BenderService
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text

object CommandBendingCopy : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.copy.base")
        .arguments(GenericArguments.player(Text.of("player")))
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

        val target: Player = args.requireOne("player")

        val srcBender: Bender = BenderService.get()[src.uniqueId]
        val targetBender: Bender = BenderService.get()[target.uniqueId]

        srcBender.clearEquipped()
        for ((index: Int, ability: Ability<*>?) in targetBender.equippedAbilities.withIndex()) {
            srcBender.setEquipped(index, ability)
        }

        return CommandResult.success()
    }
}