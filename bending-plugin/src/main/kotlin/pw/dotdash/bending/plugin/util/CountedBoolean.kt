package pw.dotdash.bending.plugin.util

@Suppress("NOTHING_TO_INLINE")
inline class CountedBoolean(val counter: Int) {

    inline operator fun invoke(): Boolean = this.counter > 0

    inline operator fun inc(): CountedBoolean {
        return CountedBoolean(this.counter + 1)
    }

    inline operator fun dec(): CountedBoolean {
        return CountedBoolean(this.counter - 1)
    }
}