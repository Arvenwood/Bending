package arvenwood.bending.api.ability

import arvenwood.bending.api.element.Element
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.CatalogType
import org.spongepowered.api.event.cause.Cause
import kotlin.coroutines.CoroutineContext

interface AbilityType<out T : Ability<T>> : CatalogType, CoroutineContext.Element {

    override fun getId(): String

    override fun getName(): String

    val element: Element

    val executionTypes: Set<AbilityExecutionType>

    val default: Ability<T>

    fun load(node: ConfigurationNode): T

    override val key: CoroutineContext.Key<*> get() = AbilityType

    companion object CoroutineKey : CoroutineContext.Key<AbilityType<Ability<*>>>
}