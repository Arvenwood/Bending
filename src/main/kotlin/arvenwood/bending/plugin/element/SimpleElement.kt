package arvenwood.bending.plugin.element

import arvenwood.bending.api.element.Element
import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.text.format.TextColors

data class SimpleElement(private val id: String, private val name: String, override val color: TextColor) : Element {
    override fun getId(): String = id
    override fun getName(): String = name

    class Builder : Element.Builder {

        private var id: String? = null
        private var name: String? = null
        private var color: TextColor? = null

        override fun id(id: String): Element.Builder {
            this.id = id
            return this
        }

        override fun name(name: String): Element.Builder {
            this.name = name
            return this
        }

        override fun color(color: TextColor): Element.Builder {
            this.color = color
            return this
        }

        override fun from(value: Element): Element.Builder {
            this.id = value.id
            this.name = value.name
            this.color = value.color
            return this
        }

        override fun reset(): Element.Builder {
            this.id = null
            this.name = null
            this.color = null
            return this
        }

        override fun build(): Element {
            val id = checkNotNull(this.id)
            return SimpleElement(
                id = id,
                name = this.name ?: id,
                color = this.color ?: TextColors.RESET
            )
        }
    }
}