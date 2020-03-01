package pw.dotdash.bending.api.event;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import pw.dotdash.bending.api.ability.Ability;
import pw.dotdash.bending.api.ability.AbilityExecutionType;
import pw.dotdash.bending.api.bender.Bender;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExecuteAbilityEvent extends AbstractEvent implements TargetPlayerEvent, Cancellable {

    private final Player targetEntity;
    private final Bender bender;
    private final Ability ability;
    private final AbilityExecutionType executionType;
    private final Cause cause;

    private boolean cancelled = false;

    public ExecuteAbilityEvent(Player targetEntity, Bender bender, Ability ability, AbilityExecutionType executionType, Cause cause) {
        this.targetEntity = checkNotNull(targetEntity, "targetEntity");
        this.bender = checkNotNull(bender, "bender");
        this.ability = checkNotNull(ability, "ability");
        this.executionType = checkNotNull(executionType, "executionType");
        this.cause = checkNotNull(cause, "cause");
    }

    @Override
    public Player getTargetEntity() {
        return this.targetEntity;
    }

    public Bender getBender() {
        return this.bender;
    }

    public Ability getAbility() {
        return this.ability;
    }

    public AbilityExecutionType getExecutionType() {
        return this.executionType;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}