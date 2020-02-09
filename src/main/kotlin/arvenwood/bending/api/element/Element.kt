package arvenwood.bending.api.element

import arvenwood.bending.api.util.catalog.CatalogTypeManager
import arvenwood.bending.api.util.catalog.catalogTypeManager
import org.spongepowered.api.CatalogType
import org.spongepowered.api.Sponge
import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.util.ResettableBuilder

interface Element : CatalogType {

    companion object : CatalogTypeManager<Element, Builder> by catalogTypeManager();

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