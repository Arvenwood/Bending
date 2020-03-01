package pw.dotdash.bending.api.ability;

import org.spongepowered.api.event.cause.Cause;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation of {@link Ability} with some methods default
 * implemented.
 */
public abstract class AbstractAbility implements Ability {

    private final AbilityType type;
    private final long cooldown;

    protected AbstractAbility(AbilityType type, long cooldown) {
        this.type = checkNotNull(type, "type");
        this.cooldown = cooldown;
    }

    @Override
    public AbilityType getType() {
        return this.type;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public void prepare(Cause cause, AbilityContext context) {

    }

    @Override
    public boolean validate(AbilityContext context) {
        return true;
    }

    @Override
    public void preempt(AbilityContext context, AbilityExecutionType executionType) {

    }

    @Override
    public void cleanup(AbilityContext context) {

    }
}