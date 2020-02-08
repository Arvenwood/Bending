package arvenwood.bending.plugin.util

import com.google.common.collect.Table
import com.google.common.collect.Tables
import java.util.*

fun <R, C, V> table(map: MutableMap<R, MutableMap<C, V>>, factory: () -> MutableMap<C, V>): Table<R, C, V> =
    Tables.newCustomTable(map, factory)

inline fun <reified K : Enum<K>, V> enumMap(): EnumMap<K, V> =
    EnumMap(K::class.java)