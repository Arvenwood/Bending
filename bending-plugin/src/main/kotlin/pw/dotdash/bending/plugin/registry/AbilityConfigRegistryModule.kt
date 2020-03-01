package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import org.spongepowered.api.registry.util.DelayedRegistration
import org.spongepowered.api.registry.util.RegistrationDependency
import pw.dotdash.bending.api.ability.AbilityConfig
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

@RegistrationDependency(AbilityTypeRegistryModule::class)
object AbilityConfigRegistryModule :
    AbstractPrefixAlternateRegistryModule<AbilityConfig>("bending"),
    AdditionalCatalogRegistryModule<AbilityConfig> {

    override fun registerAdditionalCatalog(extraCatalog: AbilityConfig) {
        this.register(extraCatalog)
    }
}