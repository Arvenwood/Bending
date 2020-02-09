package arvenwood.bending.api.element

import org.spongepowered.api.CatalogType
import org.spongepowered.api.Sponge
import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.util.ResettableBuilder

interface Element : CatalogType {

    companion object {

        @JvmStatic
        fun builder(): Builder = Sponge.getRegistry().createBuilder(Builder::class.java)

        @JvmStatic
        operator fun get(id: String): Element? = Sponge.getRegistry().getType(Element::class.java, id).orElse(null)

        @JvmStatic
        val all: Collection<Element> get() = Sponge.getRegistry().getAllOf(Element::class.java)
    }

    override fun getId(): String

    override fun getName(): String

    val color: TextColor

    interface Builder : ResettableBuilder<Element, Builder> {

        fun id(id: String): Builder

        fun name(name: String): Builder

        fun color(color: TextColor): Builder

        fun build(): Element
    }
}