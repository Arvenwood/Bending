package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import pw.dotdash.bending.api.protection.PvpProtection
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

object PvpProtectionRegistryModule :
    AbstractPrefixAlternateRegistryModule<PvpProtection>("bending"),
    AdditionalCatalogRegistryModule<PvpProtection> {

    override fun registerAdditionalCatalog(extraCatalog: PvpProtection) {
        this.register(extraCatalog)
    }
}