package arvenwood.bending.api.ability

import arvenwood.bending.api.ability.simple.SimpleAbilityContext
import org.spongepowered.api.CatalogType
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface AbilityContext : CoroutineContext.Element {

    companion object CoroutineKey : CoroutineContext.Key<AbilityContext> {
        @JvmStatic
        fun of(): AbilityContext = SimpleAbilityContext()

        @JvmStatic
        fun of(vararg pairs: Pair<Key<Any>, Any>): AbilityContext =
            SimpleAbilityContext(*pairs)
    }

    operator fun <E : Any> get(key: Key<E>): E?

    operator fun <E : Any> set(key: Key<E>, value: E)

    fun <E : Any> remove(key: Key<E>): E?

    operator fun contains(key: Key<Any>): Boolean

    abstract class Key<out E>(private val id: String, private val name: String = id) : CatalogType {
        override fun getId(): String = id
        override fun getName(): String = name
    }
}

fun <E : Any> AbilityContext.with(key: AbilityContext.Key<E>, value: E): E {
    this[key] = value
    return value
}

fun <E : Any> AbilityContext.require(key: AbilityContext.Key<E>): E =
    this[key] ?: throw NoSuchElementException(key.id)

fun <E : Any> AbilityContext.by(key: AbilityContext.Key<E>): ReadWriteProperty<Any?, E> =
    ContextDelegate(this, key)

fun <E : Any> AbilityContext.by(key: AbilityContext.Key<E>, initial: E): ReadWriteProperty<Any?, E> {
    this[key] = initial
    return this.by(key)
}

private class ContextDelegate<E : Any>(private val context: AbilityContext, private val key: AbilityContext.Key<E>) : ReadWriteProperty<Any?, E> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): E =
        this.context.require(this.key)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: E) {
        this.context[this.key] = value
    }
}