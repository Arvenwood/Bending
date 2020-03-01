package pw.dotdash.bending.api.ability;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;

/**
 * A specific configuration of an {@link AbilityType}.
 */
public interface Ability {

    /**
     * The meta information about this kind of ability.
     *
     * @return The ability metadata
     */
    AbilityType getType();

    /**
     * The duration (in milliseconds) that a bender must wait before being able
     * to use the ability again.
     *
     * <p>Note: 0 means no cooldown.</p>
     *
     * @return The ability cooldown
     */
    long getCooldown();

    /**
     * Fills in the {@link AbilityContext} from the given {@link Cause}.
     *
     * <p>
     * Already available contexts:
     * - {@link AbilityContextKeys#BENDER}
     * - {@link AbilityContextKeys#PLAYER}
     * </p>
     *
     * @param cause The cause used for filling
     * @param context The context being filled in
     */
    void prepare(Cause cause, AbilityContext context);

    /**
     * Uses the context to check for correct behavior before executing the
     * ability.
     *
     * <p>For example, protection checks are usually done here.</p>
     *
     * @param context The context used for validation
     * @return True if validation was successful
     */
    boolean validate(AbilityContext context);

    /**
     * Preemptively executes operations that must be done before the ability is
     * started.
     *
     * <p>For example, cancelling other instances of the same kind of ability
     * is done here.</p>
     *
     * @param context The context used for preemption
     * @param executionType How the ability is being executed
     */
    void preempt(AbilityContext context, AbilityExecutionType executionType);

    /**
     * Executes the ability.
     *
     * <p>For abilities that last a number of ticks, use
     * {@link org.spongepowered.api.scheduler.Task}s and set
     * {@link AbilityTask#setCurrentTask(Task)}</p>
     *
     * @param context The context used for execution
     * @param executionType How the ability is being executed
     * @param task The task used for managing long-running abilities
     */
    void execute(AbilityContext context, AbilityExecutionType executionType, AbilityTask task);

    /**
     * Handles after-execution cleanup.
     *
     * <p>For example, re-disabling flight is done here.</p>
     *
     * @param context The context used for cleanup
     */
    void cleanup(AbilityContext context);
}