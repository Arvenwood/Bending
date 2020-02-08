package arvenwood.bending.api.ability

import kotlin.coroutines.CoroutineContext

enum class AbilityExecutionType : CoroutineContext.Element {
    /**
     * The ability is activated by punching.
     */
    LEFT_CLICK,
    /**
     * The ability is activated by interacting.
     */
    RIGHT_CLICK,
    /**
     * The ability is activated by switching items in the hands.
     */
    SWAP_HAND,
    /**
     * The ability is activated by jumping.
     */
    JUMP,
    /**
     * The ability is activated by sneaking.
     */
    SNEAK,
    /**
     * The ability is activated by sprinting.
     */
    SPRINT_ON,
    /**
     * The ability is activated by stopping sprint.
     */
    SPRINT_OFF,
    /**
     * The ability is activated by falling a certain distance.
     * Fetch [StandardContext.fallDistance] from the context to get the distance.
     */
    FALL,
    /**
     * The ability is activated by combining multiple abilities.
     */
    COMBO;

    override val key: CoroutineContext.Key<*> get() = AbilityExecutionType

    companion object CoroutineKey : CoroutineContext.Key<AbilityExecutionType>
}