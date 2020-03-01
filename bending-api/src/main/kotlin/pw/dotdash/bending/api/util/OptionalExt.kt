@file:Suppress("NOTHING_TO_INLINE")

package pw.dotdash.bending.api.util

import java.util.*

inline fun <T> Optional<T>.unwrap(): T? = this.orElse(null)