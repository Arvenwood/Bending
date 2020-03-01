package pw.dotdash.bending.api.util

inline fun <reified T : Any, reified R : Any> Array<T>.mapToArray(block: (T) -> R): Array<R> {
    val result: Array<R?> = arrayOfNulls(this.size)

    for (i: Int in this.indices) {
        result[i] = block(this[i])
    }

    @Suppress("UNCHECKED_CAST")
    return result as Array<R>
}

fun <T> Array<T>.loopedIterator(): Iterator<T> = LoopedArrayIterator(this)

private data class LoopedArrayIterator<T>(private val array: Array<T>) : Iterator<T> {

    private var index: Int = 0

    override fun hasNext(): Boolean = this.index < this.array.size

    override fun next(): T {
        val value: T = this.array[this.index++]
        if (this.index == this.array.size) {
            this.index = 0
        }
        return value
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is LoopedArrayIterator<*> -> false
        else -> this.array.contentEquals(other.array)
    }

    override fun hashCode(): Int = this.array.contentHashCode()
}