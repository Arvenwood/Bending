package arvenwood.bending.api.config

import arvenwood.bending.api.ability.AbilityType
import org.spongepowered.api.Sponge

interface AbilityConfigService {

    companion object {
        @JvmStatic
        fun get(): AbilityConfigService =
            Sponge.getServiceManager().provideUnchecked(AbilityConfigService::class.java)
    }

    val all: Collection<AbilityConfig>

    operator fun get(name: String, type: AbilityType<*>): AbilityConfig?

    operator fun get(name: String): Map<AbilityType<*>, AbilityConfig>

    operator fun get(type: AbilityType<*>): Map<String, AbilityConfig>

    fun register(config: AbilityConfig)

    fun registerAll(configs: Iterable<AbilityConfig>) {
        configs.forEach(this::register)
    }

    fun registerAll(vararg configs: AbilityConfig) {
        registerAll(configs.toList())
    }
}