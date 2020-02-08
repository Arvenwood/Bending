package arvenwood.bending.plugin.util

@Suppress("NOTHING_TO_INLINE")
inline class EpochTime(val milli: Long) {

    inline fun elapsedNow(): Long = System.currentTimeMillis() - this.milli

    inline fun elapsed(current: EpochTime): Long = current.milli - this.milli

    companion object {
        inline fun now(): EpochTime = EpochTime(System.currentTimeMillis())
    }
}