package arvenwood.bending.plugin.command

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Element
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments.catalogedElement
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.text.format.TextColors.GOLD
import org.spongepowered.api.text.format.TextColors.GREEN

object CommandBendingList : CommandExecutor {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.user.list.base")
        .arguments(catalogedElement(Text.of("element"), Element::class.java))
        .executor(this)
        .build()

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val element: Element = args.requireOne("element")

        val contents: List<Text> =
            AbilityType.all.asSequence()
                .sortedBy { it.name }
                .filter { it.element === element }
                .map { it.showBindable() }
                .toList()

        val pagination: PaginationList = PaginationList.builder()
            .title(Text.of(element.color, "${element.name} Abilities"))
            .padding(Text.of(GOLD, "="))
            .contents(contents)
            .build()

        pagination.sendTo(src)

        return CommandResult.success()
    }

    private fun AbilityType<*>.showBindable(): Text =
        Text.builder(this.name)
            .color(this.element.color)
            .onHover(
                TextActions.showText(
                    Text.of(
                        this.element.color, this.description,
                        "\n\nInstructions:\n",
                        GOLD, this.instructions,
                        GREEN, "\n\nClick to bind this ability's default config!"
                    )
                )
            )
            .onClick(TextActions.runCommand("/bending bind ${this.id} default"))
            .build()
}