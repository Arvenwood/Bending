package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule
import org.spongepowered.api.registry.util.RegisterCatalog
import org.spongepowered.api.util.TypeTokens
import pw.dotdash.bending.api.ability.AbilityContextKey
import pw.dotdash.bending.api.ability.AbilityContextKeys
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule
import pw.dotdash.bending.plugin.util.BendingTypeTokens

@RegisterCatalog(AbilityContextKeys::class)
object AbilityContextKeyRegistryModule :
    AbstractPrefixAlternateRegistryModule<AbilityContextKey<*>>("bending"),
    AdditionalCatalogRegistryModule<AbilityContextKey<*>> {

    override fun registerDefaults() {
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.AFFECTED_LOCATIONS)
                .id("bending:affected_locations")
                .name("Affected Locations")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.AFFECTED_ENTITIES)
                .id("bending:affected_entities")
                .name("Affected Entities")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.BENDER_TOKEN)
                .id("bending:bender")
                .name("Bender")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.LOCATION_WORLD_TOKEN)
                .id("bending:current_location")
                .name("Current Location")
                .build()
        )
        this.register(
            AbilityContextKey.builder(TypeTokens.VECTOR_3D_TOKEN)
                .id("bending:direction")
                .name("Direction")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.ABILITY_EXECUTION_TYPE_TOKEN)
                .id("bending:execution_type")
                .name("Execution Type")
                .build()
        )
        this.register(
            AbilityContextKey.builder(TypeTokens.FLOAT_TOKEN)
                .id("bending:fall_distance")
                .name("Fall Distance")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.LOCATION_WORLD_TOKEN)
                .id("bending:origin")
                .name("Origin")
                .build()
        )
        this.register(
            AbilityContextKey.builder(BendingTypeTokens.PLAYER_TOKEN)
                .id("bending:player")
                .name("Player")
                .build()
        )
    }

    override fun registerAdditionalCatalog(extraCatalog: AbilityContextKey<*>) {
        this.register(extraCatalog)
    }
}