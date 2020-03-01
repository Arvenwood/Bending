package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import pw.dotdash.bending.api.protection.BuildProtection
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

object BuildProtectionRegistryModule :
    AbstractPrefixAlternateRegistryModule<BuildProtection>("bending"),
    AdditionalCatalogRegistryModule<BuildProtection> {

    override fun registerAdditionalCatalog(extraCatalog: BuildProtection) {
        this.register(extraCatalog)
    }
}