package pw.dotdash.bending.api.ability;

import org.spongepowered.api.scheduler.Task;
import pw.dotdash.bending.api.bender.Bender;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Represents a long-running ability that has been scheduled.
 */
public interface AbilityTask {

    /**
     * Gets the bender that executed the ability.
     *
     * @return The bender
     */
    Bender getBender();

    /**
     * Gets the ability that is being executed.
     *
     * @return The executing ability
     */
    Ability getAbility();

    /**
     * Gets the type of ability that is being executed.
     *
     * @return The type of ability
     */
    AbilityType getType();

    /**
     * Gets the ability context used for storing execution data between pauses.
     *
     * @return The ability context
     */
    AbilityContext getContext();

    /**
     * Gets how the ability was executed.
     *
     * @return The ability execution type
     */
    AbilityExecutionType getExecutionType();

    /**
     * Gets the currently scheduled {@link Task} for the ability.
     *
     * @return The scheduled sponge task
     */
    Optional<Task> getCurrentTask();

    /**
     * Sets the currently scheduled {@link Task}.
     *
     * <p>If there is a task currently running, it is cancelled and replaced.</p>
     *
     * @param task The new task to schedule for execution
     */
    void setCurrentTask(@Nullable Task task);

    /**
     * Cancels the ability and all of it's tasks.
     *
     * @return True if the ability was cancelled
     */
    boolean cancel();
}