package arvenwood.bending.api.element

import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.text.format.TextColors

object Elements {

    object Water : AbstractElement("bending:water", "Water Element") {
        override val color: TextColor = TextColors.AQUA
    }

    object Earth : AbstractElement("bending:earth", "Earth Element") {
        override val color: TextColor = TextColors.GREEN
    }

    object Fire : AbstractElement("bending:fire", "Fire Element") {
        override val color: TextColor = TextColors.RED
    }

    object Air : AbstractElement("bending:air", "Air Element") {
        override val color: TextColor = TextColors.GRAY
    }
}