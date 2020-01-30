package arvenwood.bending.api.ability

import org.spongepowered.api.entity.living.player.Player
import kotlin.coroutines.CoroutineContext

/**
 * A specific configuration of an [AbilityType].
 *
 * @param T The self type
 */
interface Ability<out T : Ability<T>> : CoroutineContext.Element {

    /**
     * The meta information about all configurations of this kind of ability.
     */
    val type: AbilityType<T>

    /**
     * The duration (in milliseconds) that a player must wait before
     * being able to use the ability again.
     *
     * Note: 0 means no cooldown.
     */
    val cooldown: Long

    /**
     * Fills in the [AbilityContext] from the given [Player].
     *
     * Note: The player is already added to the context automatically
     * under [StandardContext.player].
     *
     * @param player The player wanting to execute the ability
     * @param context The context being filled in
     */
    fun prepare(player: Player, context: AbilityContext) {}

    /**
     * Checks values in the [AbilityContext] for correct behavior
     * before executing the ability.
     *
     * For example, protection checks are usually done here.
     *
     * @param context The context used for validation
     * @return Whether the validation was successful
     */
    fun validate(context: AbilityContext): Boolean = true

    /**
     * Preemptively executes operations that must be done before
     * the ability is started.
     *
     * For example, cancelling other instances of this same type of ability
     * is done here.
     *
     * @param context The context used for preemption
     * @param executionType How the ability is being preempted
     * @return Whether the ability is ready to execute, or if there were errors
     */
    fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {}

    /**
     * Executes this particular ability configuration.
     *
     * @param context The context used during execution to store instance data
     * @param executionType How the ability is being executed
     * @return Whether the ability successfully executed, or if it errored out
     */
    suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult

    /**
     * Handles after-execution cleanup, such as re-disabling flight.
     *
     * @param context The context used for cleanup
     */
    fun cleanup(context: AbilityContext) {}

    override val key: CoroutineContext.Key<*> get() = CoroutineKey

    companion object CoroutineKey : CoroutineContext.Key<Ability<*>>
}