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
}