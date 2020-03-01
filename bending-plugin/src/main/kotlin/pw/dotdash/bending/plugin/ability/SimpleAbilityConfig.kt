package pw.dotdash.bending.plugin.ability

import pw.dotdash.bending.api.ability.AbilityConfig
import pw.dotdash.bending.api.ability.AbilityConfigLoader

data class SimpleAbilityConfig(
    private val id: String,
    private val name: String,
    private val loader: AbilityConfigLoader
) : AbilityConfig {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    override fun getLoader(): AbilityConfigLoader = this.loader

    class Builder : AbilityConfig.Builder {

        private var id: String? = null
        private var name: String? = null
        private var loader: AbilityConfigLoader? = null

        override fun id(id: String): AbilityConfig.Builder {
            this.id = id
            return this
        }

        override fun name(name: String): AbilityConfig.Builder {
            this.name = name
            return this
        }

        override fun loader(loader: AbilityConfigLoader): AbilityConfig.Builder {
            this.loader = loader
            return this
        }

        override fun build(): AbilityConfig {
            val id: String = checkNotNull(this.id)
            return SimpleAbilityConfig(
                id = id,
                name = this.name ?: id,
                loader = checkNotNull(this.loader)
            )
        }

        override fun from(value: AbilityConfig): AbilityConfig.Builder {
            this.id = value.id
            this.name = value.name
            this.loader = value.loader
            return this
        }

        override fun reset(): AbilityConfig.Builder {
            this.id = null
            this.name = null
            this.loader = null
            return this
        }
    }
}