package arvenwood.bending.api

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityJob
import arvenwood.bending.api.ability.AbilityType
import kotlinx.coroutines.Job
import org.spongepowered.api.entity.living.player.Player

interface Bender {

    /**
     * The underlying [Player] that this bender represents.
     */
    val player: Player

    /**
     * The 9 currently equipped abilities, or null if that spot has none.
     */
    val equippedAbilities: List<Ability<*>?>

    /**
     * The currently selected ability (as in the player's current slot index in the [equippedAbilities]).
     */
    var selectedAbility: Ability<*>?

    /**
     * Gets all currently running abilities from this bender.
     */
    val runningAbilities: Collection<AbilityJob>

    /**
     * Gets the currently equipped ability at the given hotbar index, if any.
     */
    fun getEquipped(hotbarIndex: Int): Ability<*>?

    /**
     * Sets the equipped ability at the given hotbar index, or null to remove one.
     */
    fun setEquipped(hotbarIndex: Int, ability: Ability<*>?)

    /**
     * Removes all equipped abilities.
     */
    fun clearEquipped()

    /**
     * Executes the given ability by the given execution type.
     */
    fun execute(ability: Ability<*>, executionType: AbilityExecutionType)

    /**
     * Waits for the bender to execute the given ability by the given execution type.
     */
    suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType)

    /**
     * Waits for the bender to execute the given ability by the given execution type.
     */
    fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Job

    /**
     * Cancels any running/waiting abilities for the given ability type.
     */
    fun cancel(type: AbilityType<*>): Boolean

    /**
     * Checks if this bender is on cooldown for the given ability.
     *
     * @param type The ability to check
     * @return Whether the ability is on cooldown for this bender
     */
    fun hasCooldown(type: AbilityType<*>): Boolean

    /**
     * Sets the duration of which this bender can't use the given ability.
     *
     * @param type The ability to set for
     * @param duration How long they can't use it for
     */
    fun setCooldown(type: AbilityType<*>, duration: Long)

    /**
     * Removes the cooldown for the given ability.
     *
     * @param type The ability to remove for
     * @return When the ability would have been usable again, or null if no cooldown
     */
    fun removeCooldown(type: AbilityType<*>): Long?
}