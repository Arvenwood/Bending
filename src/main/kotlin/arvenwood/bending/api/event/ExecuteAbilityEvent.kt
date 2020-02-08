package arvenwood.bending.api.event

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent
import org.spongepowered.api.event.impl.AbstractEvent

data class ExecuteAbilityEvent(
    private val targetEntity: Player,
    val bender: Bender,
    val abilityType: AbilityType<*>,
    val executionType: AbilityExecutionType,
    private val cause: Cause
) : AbstractEvent(), TargetPlayerEvent, Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean = this.cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    override fun getTargetEntity(): Player = this.targetEntity

    override fun getCause(): Cause = this.cause
}