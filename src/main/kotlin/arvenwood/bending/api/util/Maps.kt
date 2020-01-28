package arvenwood.bending.api.util

import java.util.*

fun <K, V> identityHashMapOf(vararg pairs: Pair<K, V>): IdentityHashMap<K, V> =
    pairs.toMap(IdentityHashMap(mapCapacity(pairs.size)))

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
