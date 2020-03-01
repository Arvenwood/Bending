package pw.dotdash.bending.api.event;

import com.google.common.base.Optional;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import pw.dotdash.bending.api.ability.AbilityType;

import javax.annotation.Nullable;

import java.util.OptionalLong;

import static com.google.common.base.Preconditions.checkNotNull;

public class SetCooldownEvent extends AbstractEvent implements TargetPlayerEvent, Cancellable {

    private final Player targetEntity;
    private final AbilityType abilityType;
    @Nullable
    private final Long cooldownMilli;
    private final Cause cause;

    public SetCooldownEvent(Player targetEntity, AbilityType abilityType, @Nullable Long cooldownMilli, Cause cause) {
        this.targetEntity = checkNotNull(targetEntity, "targetEntity");
        this.abilityType = checkNotNull(abilityType, "abilityType");
        this.cooldownMilli = cooldownMilli;
        this.cause = checkNotNull(cause, "cause");
    }

    private boolean cancelled = false;

    @Override
    public Player getTargetEntity() {
        return this.targetEntity;
    }

    public AbilityType getAbilityType() {
        return this.abilityType;
    }

    public OptionalLong getCooldownMilli() {
        return this.cooldownMilli == null ? OptionalLong.empty() : OptionalLong.of(this.cooldownMilli);
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