package arvenwood.bending.api.element

import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.text.format.TextColors

object Elements {

    object Air : AbstractElement("bending:air", "Air Element") {
        override val color: TextColor = TextColors.GRAY
    }

    object Fire : AbstractElement("bending:fire", "Fire Element") {
        override val color: TextColor = TextColors.RED
    }
}