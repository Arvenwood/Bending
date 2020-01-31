package arvenwood.bending.api.util

@Suppress("NOTHING_TO_INLINE")
inline class StackableBoolean(val counter: Int) {

    inline operator fun invoke(): Boolean = this.counter > 0

    inline operator fun inc(): StackableBoolean {
        return StackableBoolean(this.counter + 1)
    }

    inline operator fun dec(): StackableBoolean {
        return StackableBoolean(this.counter - 1)
    }

    inline fun with(block: () -> Unit) {

    }
}