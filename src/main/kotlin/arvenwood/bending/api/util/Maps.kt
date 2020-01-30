package arvenwood.bending.api.util

import com.google.common.collect.Sets
import java.util.*

fun <K, V> identityHashMapOf(vararg pairs: Pair<K, V>): IdentityHashMap<K, V> =
    pairs.toMap(IdentityHashMap(mapCapacity(pairs.size)))

fun <E> identityHashSetOf(vararg values: E): MutableSet<E> =
    Sets.newIdentityHashSet<E>().apply { addAll(values) }

fun <E : Enum<E>> enumSetOf(e: E) : MutableSet<E> =
    EnumSet.of(e)

fun <E : Enum<E>> enumSetOf(e1: E, e2: E) : MutableSet<E> =
    EnumSet.of(e1, e2)

fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E) : MutableSet<E> =
    EnumSet.of(e1, e2, e3)

fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E, e4: E) : MutableSet<E> =
    EnumSet.of(e1, e2, e3, e4)

fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E, e4: E, e5: E) : MutableSet<E> =
    EnumSet.of(e1, e2, e3, e4, e5)

fun <E : Enum<E>> enumSetOf(first: E, vararg rest: E) : MutableSet<E> =
    EnumSet.of(first, *rest)

private const val INT_MAX_POWER_OF_TWO: Int = Int.MAX_VALUE / 2 + 1

internal fun mapCapacity(expectedSize: Int): Int {
    if (expectedSize < 3) {
        return expectedSize + 1
    }
    if (expectedSize < INT_MAX_POWER_OF_TWO) {
        return expectedSize + expectedSize / 3
    }
    return Int.MAX_VALUE // any large value
}
