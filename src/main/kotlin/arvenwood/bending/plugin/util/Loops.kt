package arvenwood.bending.plugin.util

inline fun whileInclusive(from: Double, to: Double, step: Double, block: (Double) -> Unit) {
    var current: Double = from
    while (current <= to) {
        block(current)
        current += step
    }
}

inline fun whileExclusive(from: Double, to: Double, step: Double, block: (Double) -> Unit) {
    var current: Double = from
    while (current < to) {
        block(current)
        current += step
    }
}