package arvenwood.bending.api.config.simple

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import ninja.leaping.configurate.ConfigurationNode

data class SimpleAbilityConfig(
    override val name: String,
    override val type: AbilityType<*>,
    override val node: ConfigurationNode
) : AbilityConfig {

    override val ability: Ability<*> by lazy {
        this.type.load(this.node)
    }
}