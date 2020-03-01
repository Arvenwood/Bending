package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import org.spongepowered.api.registry.util.RegisterCatalog
import org.spongepowered.api.text.format.TextColors
import pw.dotdash.bending.api.element.Element
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

@RegisterCatalog(Elements::class)
object ElementRegistryModule :
    AbstractPrefixAlternateRegistryModule<Element>("bending"),
    AdditionalCatalogRegistryModule<Element> {

    override fun registerDefaults() {
        this.register(
            Element.builder()
                .id("bending:air")
                .name("Air")
                .color(TextColors.GRAY)
                .primaryParticleType(ParticleTypes.CLOUD)
                .build()
        )
        this.register(
            Element.builder()
                .id("bending:chi")
                .name("Chi")
                .color(TextColors.GOLD)
                .primaryParticleType(ParticleTypes.CRITICAL_HIT)
                .build()
        )
        this.register(
            Element.builder()
                .id("bending:earth")
                .name("Earth")
                .color(TextColors.GREEN)
                .primaryParticleType(ParticleTypes.BLOCK_CRACK)
                .build()
        )
        this.register(
            Element.builder()
                .id("bending:fire")
                .name("Fire")
                .color(TextColors.RED)
                .primaryParticleType(ParticleTypes.FLAME)
                .build()
        )
        this.register(
            Element.builder()
                .id("bending:water")
                .name("Water")
                .color(TextColors.AQUA)
                .primaryParticleType(ParticleTypes.WATER_BUBBLE)
                .build()
        )
    }

    override fun registerAdditionalCatalog(extraCatalog: Element) {
        this.register(extraCatalog)
    }
}