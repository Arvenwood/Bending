package pw.dotdash.bending.plugin.util

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContextKey
import org.spongepowered.api.text.Text

fun Cause.print(player: Player) {
    player.sendMessage(Text.of("-- Cause Stack:"))
    for ((index: Int, value: Any?) in this.all().withIndex()) {
        player.sendMessage(Text.of("  $index: $value (${value::class})"))
    }
    player.sendMessage(Text.of("-- Cause Context:"))
    for ((key: EventContextKey<*>, value: Any?) in this.context.asMap()) {
        player.sendMessage(Text.of("  ${key.id}: $value (${value::class})"))
    }
}