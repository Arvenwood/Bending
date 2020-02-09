package arvenwood.bending.plugin.command

import org.spongepowered.api.command.spec.CommandSpec

object CommandBending {

    val SPEC: CommandSpec = CommandSpec.builder()
        .permission("bending.base")
        .child(CommandBendingBind.SPEC, "bind", "b")
        .child(CommandBendingClear.SPEC, "clear")
        .child(CommandBendingCopy.SPEC, "copy")
        .build()
}