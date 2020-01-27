package arvenwood.bending.plugin.registry

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.ability.air.AirBlastAbility
import arvenwood.bending.api.ability.air.AirJumpAbility
import arvenwood.bending.api.ability.air.AirShieldAbility
import arvenwood.bending.api.ability.fire.*

class AbilityTypeCatalogRegisterModule : HashMapCatalogRegistryModule<AbilityType<*>>() {
    init {
        this.registerAdditionalCatalog(AirBlastAbility)
        this.registerAdditionalCatalog(AirJumpAbility)
        this.registerAdditionalCatalog(AirShieldAbility)

        this.registerAdditionalCatalog(CombustionAbility)
        this.registerAdditionalCatalog(FireBlastAbility)
        this.registerAdditionalCatalog(FireJetAbility)
        this.registerAdditionalCatalog(FireShieldAbility)
        this.registerAdditionalCatalog(FireWallAbility)
    }
}