package arvenwood.bending.api.ability

import org.spongepowered.api.entity.living.player.Player
import kotlin.coroutines.CoroutineContext

interface Ability<out T : Ability<T>> : CoroutineContext.Element {

    val type: AbilityType<T>

    val cooldown: Long

    fun prepare(player: Player, context: AbilityContext) {}

    fun shouldExecute(context: AbilityContext): Boolean = true

    suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult

    fun cleanup(context: AbilityContext) {}

    override val key: CoroutineContext.Key<*> get() = CoroutineKey

    companion object CoroutineKey : CoroutineContext.Key<Ability<*>>
}