package pw.dotdash.bending.plugin.command

import org.spongepowered.api.command.spec.CommandSpec

object CommandBending {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.base")
        .child(CommandBendingBind.SPEC, "bind", "b")
        .child(CommandBendingClear.SPEC, "clear")
        .child(CommandBendingCopy.SPEC, "copy")
        .child(CommandBendingDisplay.SPEC, "display", "d")
        .child(CommandBendingList.SPEC, "list", "l")
        .build()
}