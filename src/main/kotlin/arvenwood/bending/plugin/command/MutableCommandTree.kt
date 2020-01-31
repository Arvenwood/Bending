package arvenwood.bending.plugin.command

import director.core.MutableCommandTree
import director.core.Parameter
import director.sponge.requirePermission
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource

fun <S : CommandSource, V> MutableCommandTree<S, V, CommandResult>.child(
    alias: String,
    permission: String
): MutableCommandTree.Child<S, V, CommandResult> =
    this.child(alias).apply {
        this.requirePermission(permission)
    }

fun <S : CommandSource, V> MutableCommandTree<S, V, CommandResult>.child(
    vararg aliases: String,
    permission: String
): MutableCommandTree.Child<S, V, CommandResult> =
    this.child(*aliases).apply {
        this.requirePermission(permission)
    }

fun <S : CommandSource, V> MutableCommandTree<S, V, CommandResult>.child(
    aliases: List<String>,
    permission: String
): MutableCommandTree.Child<S, V, CommandResult> =
    this.child(aliases).apply {
        this.requirePermission(permission)
    }

fun <S : CommandSource, V, NV> MutableCommandTree<S, V, CommandResult>.argument(
    parameter: Parameter<S, V, NV>,
    permission: String
): MutableCommandTree.Argument<S, V, NV, CommandResult> =
    this.argument(parameter).apply {
        this.requirePermission(permission)
    }