package arvenwood.bending.api

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityJob
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.util.StackableBoolean
import kotlinx.coroutines.Job
import kotlin.reflect.KClass

interface Bender {

    var flight: StackableBoolean

    var selectedAbility: Ability<*>?

    val equippedAbilities: Map<Int, Ability<*>>

    operator fun get(hotbarIndex: Int): Ability<*>?

    operator fun set(hotbarIndex: Int, ability: Ability<*>?)

    fun clearEquipped()

    val runningAbilities: Collection<AbilityJob>

    fun execute(ability: Ability<*>, executionType: AbilityExecutionType)

    suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType)

    fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Job

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