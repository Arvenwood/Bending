package arvenwood.bending.api.ability

import arvenwood.bending.api.element.Element
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.CatalogType
import org.spongepowered.api.text.Text
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface AbilityType<out T : Ability<T>> : CatalogType, CoroutineContext.Element {

    override fun getId(): String

    override fun getName(): String

    val instructions: Text get() = Text.EMPTY

    val description: Text get() = Text.EMPTY

    val element: Element

    val executionTypes: Set<AbilityExecutionType>

    fun load(node: ConfigurationNode): T

    override val key: CoroutineContext.Key<*> get() = AbilityType

    companion object CoroutineKey : CoroutineContext.Key<AbilityType<Ability<*>>>
}