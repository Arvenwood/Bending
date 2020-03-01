package pw.dotdash.bending.plugin.ability

import com.google.common.reflect.TypeToken
import pw.dotdash.bending.api.ability.AbilityContextKey

data class SimpleAbilityContextKey<E>(
    private val id: String,
    private val name: String,
    private val type: TypeToken<E>
) : AbilityContextKey<E> {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    override fun getAllowedType(): TypeToken<E> = this.type

    class Builder<E> : AbilityContextKey.Builder<E> {

        private var id: String? = null
        private var name: String? = null
        private var type: TypeToken<E>? = null

        override fun type(type: TypeToken<E>): AbilityContextKey.Builder<E> {
            this.type = type
            return this
        }

        override fun id(id: String): AbilityContextKey.Builder<E> {
            this.id = id
            return this
        }

        override fun name(name: String): AbilityContextKey.Builder<E> {
            this.name = name
            return this
        }

        override fun build(): AbilityContextKey<E> {
            val id: String = checkNotNull(this.id)
            return SimpleAbilityContextKey(
                id = id,
                name = this.name ?: id,
                type = checkNotNull(this.type)
            )
        }

        override fun from(value: AbilityContextKey<E>): AbilityContextKey.Builder<E> =
            throw UnsupportedOperationException()

        override fun reset(): AbilityContextKey.Builder<E> {
            this.id = null
            this.name = null
            this.type = null
            return this
        }
    }
}