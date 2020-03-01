package pw.dotdash.bending.plugin.ability

import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.element.Element
import com.google.common.collect.Sets
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.util.unwrap
import java.util.*
import java.util.function.Function

data class SimpleAbilityType(
    private val id: String,
    private val name: String,
    private val element: Element,
    private val executionTypes: Set<AbilityExecutionType>,
    private val instructions: Text?,
    private val description: Text?,
    private val loader: Function<ConfigurationNode, Optional<Ability>>
) : AbilityType {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    override fun getElement(): Element = this.element

    override fun getExecutionTypes(): Collection<AbilityExecutionType> = this.executionTypes

    override fun getInstructions(): Optional<Text> = Optional.ofNullable(this.instructions)

    override fun getDescription(): Optional<Text> = Optional.ofNullable(this.description)

    override fun show(): Text =
        Text.builder(this.name)
            .color(this.element.color)
            .onHover(TextActions.showText(Text.of(this.element.color, this.description, "\n\nInstructions:\n", TextColors.GOLD, this.instructions)))
            .build()

    override fun load(node: ConfigurationNode): Optional<Ability> = this.loader.apply(node)

    class Builder : AbilityType.Builder {

        private var id: String? = null
        private var name: String? = null
        private var element: Element? = null
        private var executionTypes: Set<AbilityExecutionType>? = null
        private var instructions: Text? = null
        private var description: Text? = null
        private var configLoader: Function<ConfigurationNode, Optional<Ability>>? = null

        override fun id(id: String): AbilityType.Builder {
            this.id = id
            return this
        }

        override fun name(name: String): AbilityType.Builder {
            this.name = name
            return this
        }

        override fun element(element: Element): AbilityType.Builder {
            this.element = element
            return this
        }

        override fun executionTypes(executionTypes: Collection<AbilityExecutionType>): AbilityType.Builder {
            this.executionTypes = executionTypes.toCollection(Sets.newIdentityHashSet())
            return this
        }

        override fun executionTypes(vararg executionTypes: AbilityExecutionType): AbilityType.Builder {
            this.executionTypes = executionTypes.toCollection(Sets.newIdentityHashSet())
            return this
        }

        override fun instructions(instructions: Text): AbilityType.Builder {
            this.instructions = instructions
            return this
        }

        override fun description(description: Text): AbilityType.Builder {
            this.description = description
            return this
        }

        override fun configLoader(configLoader: Function<ConfigurationNode, Optional<Ability>>): AbilityType.Builder {
            this.configLoader = configLoader
            return this
        }

        override fun from(value: AbilityType): AbilityType.Builder {
            this.id = value.id
            this.name = value.name
            this.element = value.element
            this.executionTypes = value.executionTypes.toCollection(Sets.newIdentityHashSet())
            this.instructions = value.instructions.unwrap()
            this.description = value.description.unwrap()
            this.configLoader = Function(value::load)
            return this
        }

        override fun reset(): AbilityType.Builder {
            this.id = null
            this.name = null
            this.element = null
            this.executionTypes = null
            this.instructions = null
            this.description = null
            this.configLoader = null
            return this
        }

        override fun build(): AbilityType {
            val id = checkNotNull(this.id)
            return SimpleAbilityType(
                id = id,
                name = this.name ?: id,
                element = checkNotNull(this.element),
                executionTypes = checkNotNull(this.executionTypes),
                instructions = this.instructions ?: Text.EMPTY,
                description = this.description ?: Text.EMPTY,
                loader = checkNotNull(this.configLoader)
            )
        }
    }
}