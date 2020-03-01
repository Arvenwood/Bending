package pw.dotdash.bending.plugin.util

import com.google.common.collect.Table

operator fun <R, C, V> Table<R, C, V>.set(rowKey: R, columnKey: C, value: V) {
    this.put(rowKey, columnKey, value)
}