package pw.dotdash.bending.api.util

inline fun <reified T : Any, reified R : Any> Array<T>.mapToArray(block: (T) -> R): Array<R> {
    val result: Array<R?> = arrayOfNulls(this.size)

    for (i: Int in this.indices) {
        result[i] = block(this[i])
    }

    @Suppress("UNCHECKED_CAST")
    return result as Array<R>
}