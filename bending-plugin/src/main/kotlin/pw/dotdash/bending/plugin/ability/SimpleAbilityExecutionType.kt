package pw.dotdash.bending.plugin.ability

import pw.dotdash.bending.api.ability.AbilityExecutionType

data class SimpleAbilityExecutionType(
    private val id: String,
    private val name: String
) : AbilityExecutionType {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    class Builder : AbilityExecutionType.Builder {

        private var id: String? = null
        private var name: String? = null

        override fun id(id: String): AbilityExecutionType.Builder {
            this.id = id
            return this
        }

        override fun name(name: String): AbilityExecutionType.Builder {
            this.name = name
            return this
        }

        override fun from(value: AbilityExecutionType): AbilityExecutionType.Builder {
            this.id = value.id
            this.name = value.name
            return this
        }

        override fun reset(): AbilityExecutionType.Builder {
            this.id = null
            this.name = null
            return this
        }

        override fun build(): AbilityExecutionType {
            val id = checkNotNull(this.id)
            return SimpleAbilityExecutionType(
                id = id,
                name = this.name ?: id
            )
        }
    }
}