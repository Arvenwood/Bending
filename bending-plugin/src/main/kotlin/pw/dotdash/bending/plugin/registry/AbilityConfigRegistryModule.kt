package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import pw.dotdash.bending.api.ability.AbilityConfig
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

object AbilityConfigRegistryModule :
    AbstractPrefixAlternateRegistryModule<AbilityConfig>("bending"),
    AdditionalCatalogRegistryModule<AbilityConfig> {

    override fun registerAdditionalCatalog(extraCatalog: AbilityConfig) {
        this.register(extraCatalog)
    }
}