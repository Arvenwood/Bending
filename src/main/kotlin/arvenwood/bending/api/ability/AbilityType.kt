package arvenwood.bending.api.ability

import arvenwood.bending.api.element.Element
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.CatalogType
import org.spongepowered.api.Sponge
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextAction
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.util.ResettableBuilder
import kotlin.coroutines.CoroutineContext

interface AbilityType<out T : Ability<T>> : CatalogType, CoroutineContext.Element {

    companion object : CoroutineContext.Key<AbilityType<Ability<*>>> {

        @JvmStatic
        fun <T : Ability<T>> builder(): Builder<T> =
            @Suppress("UNCHECKED_CAST")
            (Sponge.getRegistry().createBuilder(Builder::class.java) as Builder<T>)

        operator fun get(id: String): AbilityType<*>? =
            Sponge.getRegistry().getType(AbilityType::class.java, id).orElse(null)

        val all: Collection<AbilityType<*>>
            get() = Sponge.getRegistry().getAllOf(AbilityType::class.java)
    }

    override fun getId(): String

    override fun getName(): String

    val element: Element

    val executionTypes: Set<AbilityExecutionType>

    val instructions: Text

    val description: Text

    fun load(node: ConfigurationNode): T

    fun show(): Text =
        Text.builder(this.name)
            .color(this.element.color)
            .onHover(TextActions.showText(Text.of(this.element.color, this.description, "\n\nInstructions:\n", TextColors.GOLD, this.instructions)))
            .build()

    override val key: CoroutineContext.Key<*> get() = AbilityType

    interface Builder<out T : Ability<T>> : ResettableBuilder<AbilityType<@UnsafeVariance T>, Builder<@UnsafeVariance T>> {

        fun id(id: String): Builder<T>

        fun name(name: String): Builder<T>

        fun element(element: Element): Builder<T>

        fun executionTypes(executionTypes: Set<AbilityExecutionType>): Builder<T>

        fun executionTypes(vararg executionTypes: AbilityExecutionType): Builder<T> =
            executionTypes(executionTypes.toSet())

        fun instructions(instructions: Text): Builder<T>

        fun description(description: Text): Builder<T>

        fun loader(loader: (ConfigurationNode) -> @UnsafeVariance T): Builder<T>

        fun build(): AbilityType<T>
    }
}