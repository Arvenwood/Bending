package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

object AbilityTypeRegistryModule :
    AbstractPrefixAlternateRegistryModule<AbilityType>("bending"),
    AdditionalCatalogRegistryModule<AbilityType> {

    override fun registerAdditionalCatalog(extraCatalog: AbilityType) {
        this.register(extraCatalog)
    }
}