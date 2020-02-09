package arvenwood.bending.api.element

import org.spongepowered.api.text.format.TextColors

object Elements {

    val WATER: Element = Element.builder()
        .id("bending:water")
        .name("Water")
        .color(TextColors.AQUA)
        .build()

    val EARTH: Element = Element.builder()
        .id("bending:earth")
        .name("Earth")
        .color(TextColors.GREEN)
        .build()

    val FIRE: Element = Element.builder()
        .id("bending:fire")
        .name("Fire")
        .color(TextColors.RED)
        .build()

    val AIR: Element = Element.builder()
        .id("bending:air")
        .name("Air")
        .color(TextColors.GRAY)
        .build()

    val CHI: Element = Element.builder()
        .id("bending:chi")
        .name("Chi")
        .color(TextColors.GOLD)
        .build()
}