package arvenwood.bending.api.element

import org.spongepowered.api.CatalogType
import org.spongepowered.api.text.format.TextColor

interface Element : CatalogType {
    override fun getId(): String
    override fun getName(): String

    val color: TextColor
}