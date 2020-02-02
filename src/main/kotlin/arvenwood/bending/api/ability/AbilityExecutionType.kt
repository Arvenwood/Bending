package arvenwood.bending.api.ability

import kotlin.coroutines.CoroutineContext

enum class AbilityExecutionType : CoroutineContext.Element {

    LEFT_CLICK,

    RIGHT_CLICK,

    JUMP,

    SNEAK,

    SPRINT,

    FALL;

    override val key: CoroutineContext.Key<*> get() = AbilityExecutionType

    companion object CoroutineKey : CoroutineContext.Key<AbilityExecutionType>
}