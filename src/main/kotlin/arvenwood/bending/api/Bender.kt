package arvenwood.bending.api

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecution
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import kotlinx.coroutines.Deferred

interface Bender {

    var selectedAbility: Ability<*>?

    operator fun get(hotbarIndex: Int): Ability<*>?

    operator fun set(hotbarIndex: Int, ability: Ability<*>?)

    val runningAbilities: Collection<AbilityExecution>

    fun execute(ability: Ability<*>, executionType: AbilityExecutionType)

    suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType)

    fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Deferred<Unit>
}