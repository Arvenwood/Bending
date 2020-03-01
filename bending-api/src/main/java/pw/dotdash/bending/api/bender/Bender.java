package pw.dotdash.bending.api.bender;

import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;
import pw.dotdash.bending.api.ability.Ability;
import pw.dotdash.bending.api.ability.AbilityExecutionType;
import pw.dotdash.bending.api.ability.AbilityTask;
import pw.dotdash.bending.api.ability.AbilityType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A Bender represents an in-game entity that can execute bending abilities.
 */
public interface Bender extends Identifiable, DataSerializable {

    /**
     * Gets the player associated with the bender.
     *
     * @return The associated player, if available
     */
    Optional<Player> getPlayer();

    /**
     * Gets the ability hotbar that directly maps to the bender's real hotbar.
     *
     * @return The ability hotbar
     */
    List<Optional<Ability>> getHotbar();

    /**
     * Gets the currently selected ability.
     *
     * @return The selected ability, if available
     */
    Optional<Ability> getSelectedAbility();

    /**
     * Sets the currently selected ability.
     *
     * @param ability The ability to set
     * @return True if the operation was successful
     */
    boolean setSelectedAbility(@Nullable Ability ability);

    /**
     * Gets the ability equipped at the given hotbar index.
     *
     * <p>Note: index must be within zero to eight.</p>
     *
     * @param hotbarIndex The index to query
     * @return The equipped ability, if available
     */
    Optional<Ability> getEquippedAbility(int hotbarIndex);

    /**
     * Sets the ability equipped at the given hotbar index.
     *
     * <p>Note: index must be within zero to eight.</p>
     *
     * @param hotbarIndex The index to set at
     * @param ability     The ability to set
     * @return True if the operation was successful
     */
    boolean setEquippedAbility(int hotbarIndex, @Nullable Ability ability);

    /**
     * Removes all equipped abilities.
     */
    void clearEquippedAbilities();

    /**
     * Gets all currently running abilities' {@link AbilityTask}s.
     *
     * @return All currently running abilities
     */
    Collection<AbilityTask> getRunningAbilities();

    /**
     * Executes the ability by the given execution type.
     *
     * @param ability       The ability to execute
     * @param executionType The way to execute the ability
     * @return The ability task, if successful
     */
    Optional<AbilityTask> execute(Ability ability, AbilityExecutionType executionType);

    CompletableFuture<Void> waitForExecution(AbilityType type, AbilityExecutionType executionType);

    boolean cancel(AbilityType type);

    boolean hasCooldown(AbilityType type);

    boolean setCooldown(AbilityType type, long durationMilli);

    long removeCooldown(AbilityType type);
}