package arvenwood.bending.api.config

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityType
import ninja.leaping.configurate.ConfigurationNode

interface AbilityConfig {

    val name: String

    val type: AbilityType<*>

    val node: ConfigurationNode

    val ability: Ability<*>
}