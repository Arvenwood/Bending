package arvenwood.bending.plugin.command

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigService
import director.core.RTuple
import director.core.component1
import director.core.token.CommandTokens
import director.core.value.ValueParameter

fun abilityConfig(): ValueParameter<Any?, RTuple<AbilityType<*>, *>, AbilityConfig> =
    AbilityConfigParameter

private object AbilityConfigParameter : ValueParameter<Any?, RTuple<AbilityType<*>, *>, AbilityConfig> {

    override fun parse(source: Any?, tokens: CommandTokens, previous: RTuple<AbilityType<*>, *>): AbilityConfig {
        val (type: AbilityType<*>) = previous
        val name = tokens.next()

        return AbilityConfigService.get()[name, type]
            ?: throw tokens.newException("Unknown config '$name' for ability '${type.name}'")
    }

    override fun complete(source: Any?, tokens: CommandTokens, previous: RTuple<AbilityType<*>, *>): List<String> {
        val (type: AbilityType<*>) = previous
        return AbilityConfigService.get()[type].keys.toList()
    }
}