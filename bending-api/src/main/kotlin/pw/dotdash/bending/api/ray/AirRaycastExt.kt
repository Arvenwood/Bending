package pw.dotdash.bending.api.ray

import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player

fun Raycast.pushEntity(
    source: Player, target: Entity, canPushSelf: Boolean,
    knockbackSelf: Double, knockbackOther: Double
): Boolean =
    AirRaycast.pushEntity(this, source, target, canPushSelf, knockbackSelf, knockbackOther)