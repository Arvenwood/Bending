package arvenwood.bending.plugin.ability

import arvenwood.bending.api.ability.AbilityContext
import java.util.*
import kotlin.coroutines.CoroutineContext

class SimpleAbilityContext() : AbilityContext {
    override val key: CoroutineContext.Key<*> get() = AbilityContext

    private val map = IdentityHashMap<AbilityContext.Key<Any>, Any>()

    constructor(vararg pairs: Pair<AbilityContext.Key<Any>, Any>) : this() {
        for ((key, value) in pairs) {
            this.map[key] = value
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: AbilityContext.Key<T>): T? =
        map[key] as T?

    override fun <T : Any> set(key: AbilityContext.Key<T>, value: T) {
        map[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> remove(key: AbilityContext.Key<T>): T? =
        map.remove(key) as T?

    override fun contains(key: AbilityContext.Key<Any>): Boolean =
        key in map
}