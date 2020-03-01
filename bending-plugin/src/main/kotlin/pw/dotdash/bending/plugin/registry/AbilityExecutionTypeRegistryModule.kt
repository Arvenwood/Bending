package pw.dotdash.bending.plugin.registry

import org.spongepowered.api.registry.util.RegisterCatalog
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.AbilityExecutionTypes
import pw.dotdash.bending.plugin.registry.util.AbstractPrefixAlternateRegistryModule

@RegisterCatalog(AbilityExecutionTypes::class)
object AbilityExecutionTypeRegistryModule :
    AbstractPrefixAlternateRegistryModule<AbilityExecutionType>("bending") {

    override fun registerDefaults() {
        this.register(
            AbilityExecutionType.builder()
                .id("bending:combo")
                .name("Combo")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:fall")
                .name("Fall")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:jump")
                .name("Jump")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:left_click")
                .name("Left Click")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:passive")
                .name("Passive")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:sneak")
                .name("Sneak")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:sprint_off")
                .name("Sprint Off")
                .build()
        )
        this.register(
            AbilityExecutionType.builder()
                .id("bending:sprint_on")
                .name("Sprint On")
                .build()
        )
    }
}