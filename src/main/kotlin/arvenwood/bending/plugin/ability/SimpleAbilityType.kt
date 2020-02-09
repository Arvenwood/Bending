package arvenwood.bending.plugin.ability

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Element
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.text.Text
import java.util.*

data class SimpleAbilityType<out T : Ability<T>>(
    private val id: String,
    private val name: String,
    override val element: Element,
    override val executionTypes: Set<AbilityExecutionType>,
    override val instructions: Text,
    override val description: Text,
    private val loader: (ConfigurationNode) -> T
) : AbilityType<T> {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    override fun load(node: ConfigurationNode): T = this.loader(node)

    class Builder<out T : Ability<T>> : AbilityType.Builder<T> {

        private var id: String? = null
        private var name: String? = null
        private var element: Element? = null
        private var executionTypes: Set<AbilityExecutionType>? = null
        private var instructions: Text? = null
        private var description: Text? = null
        private var loader: ((ConfigurationNode) -> T)? = null

        override fun id(id: String): AbilityType.Builder<T> {
            this.id = id
            return this
        }

        override fun name(name: String): AbilityType.Builder<T> {
            this.name = name
            return this
        }

        override fun element(element: Element): AbilityType.Builder<T> {
            this.element = element
            return this
        }

        override fun executionTypes(executionTypes: Set<AbilityExecutionType>): AbilityType.Builder<T> {
            this.executionTypes = EnumSet.copyOf(executionTypes)
            return this
        }

        override fun instructions(instructions: Text): AbilityType.Builder<T> {
            this.instructions = instructions
            return this
        }

        override fun description(description: Text): AbilityType.Builder<T> {
            this.description = description
            return this
        }

        override fun loader(loader: (ConfigurationNode) -> @UnsafeVariance T): AbilityType.Builder<T> {
            this.loader = loader
            return this
        }

        override fun from(value: AbilityType<@UnsafeVariance T>): AbilityType.Builder<T> {
            this.id = value.id
            this.name = value.name
            this.element = value.element
            this.executionTypes = EnumSet.copyOf(value.executionTypes)
            this.instructions = value.instructions
            this.description = value.description
            this.loader = value::load
            return this
        }

        override fun reset(): AbilityType.Builder<T> {
            this.id = null
            this.name = null
            this.element = null
            this.executionTypes = null
            this.instructions = null
            this.description = null
            this.loader = null
            return this
        }

        override fun build(): AbilityType<T> {
            val id = checkNotNull(this.id)
            return SimpleAbilityType(
                id = id,
                name = this.name ?: id,
                element = checkNotNull(this.element),
                executionTypes = checkNotNull(this.executionTypes),
                instructions = this.instructions ?: Text.EMPTY,
                description = this.description ?: Text.EMPTY,
                loader = checkNotNull(this.loader)
            )
        }
    }
}