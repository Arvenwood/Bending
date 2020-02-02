package arvenwood.bending.api.ability

import kotlin.coroutines.CoroutineContext

sealed class AbilityExecutionType : CoroutineContext.Element {

    object LeftClick : AbilityExecutionType()

    object RightClick : AbilityExecutionType()

    object Jump : AbilityExecutionType()

    object Sneak : AbilityExecutionType()

    object Sprint : AbilityExecutionType()

    data class Fall(val damage: Double) : AbilityExecutionType()

    override val key: CoroutineContext.Key<*> get() = AbilityExecutionType

    companion object CoroutineKey : CoroutineContext.Key<AbilityExecutionType>
}