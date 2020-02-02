package arvenwood.bending.plugin.config

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigService
import com.google.common.collect.Table
import com.google.common.collect.Tables
import java.util.*
import kotlin.collections.HashMap

class SimpleAbilityConfigService : AbilityConfigService {

    private val configs: Table<String, AbilityType<*>, AbilityConfig> =
        Tables.newCustomTable<String, AbilityType<*>, AbilityConfig>(HashMap()) { IdentityHashMap() }

    override val all: Collection<AbilityConfig>
        get() = this.configs.values()

    override fun get(name: String, type: AbilityType<*>): AbilityConfig? =
        this.configs[name, type]

    override fun get(name: String): Map<AbilityType<*>, AbilityConfig> =
        this.configs.row(name)

    override fun get(type: AbilityType<*>): Map<String, AbilityConfig> =
        this.configs.column(type)

    override fun register(config: AbilityConfig) {
        this.configs.put(config.name, config.type, config)
    }
}