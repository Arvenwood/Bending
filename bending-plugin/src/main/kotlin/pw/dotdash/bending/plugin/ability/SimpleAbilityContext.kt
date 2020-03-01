package pw.dotdash.bending.plugin.ability

import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKey
import java.util.*
import kotlin.NoSuchElementException

class SimpleAbilityContext() : AbilityContext {
    private val map = IdentityHashMap<AbilityContextKey<out Any>, Any>()

    constructor(map: Map<AbilityContextKey<out Any>, Any>) : this() {
        this.map.putAll(map)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Any> get(key: AbilityContextKey<E>): Optional<E> =
        Optional.ofNullable(this.map[key] as E?)

    @Suppress("UNCHECKED_CAST")
    override fun <E : Any?> require(key: AbilityContextKey<E>): E =
        (this.map[key] as E?) ?: throw NoSuchElementException(key.toString())

    override fun <E : Any> set(key: AbilityContextKey<E>, value: E) {
        this.map[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Any> remove(key: AbilityContextKey<E>): Optional<E> =
        Optional.ofNullable(this.map.remove(key) as E?)

    override fun contains(key: AbilityContextKey<*>): Boolean =
        key in this.map

    class Builder : AbilityContext.Builder {

        private val entries = HashMap<AbilityContextKey<out Any>, Any>()

        override fun <E : Any> add(key: AbilityContextKey<E>, value: E): AbilityContext.Builder {
            this.entries[key] = value
            return this
        }

        override fun from(value: AbilityContext): AbilityContext.Builder {
            throw UnsupportedOperationException()
        }

        override fun reset(): AbilityContext.Builder {
            this.entries.clear()
            return this
        }

        override fun build(): AbilityContext =
            SimpleAbilityContext(this.entries)
    }
}