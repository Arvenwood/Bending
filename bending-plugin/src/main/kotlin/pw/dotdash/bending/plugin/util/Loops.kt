package pw.dotdash.bending.plugin.util

inline fun forInclusive(from: Double, to: Double, step: Double, block: (Double) -> Unit) {
    var current: Double = from
    while (current <= to) {
        block(current)
        current += step
    }
}

inline fun forExclusive(from: Double, to: Double, step: Double, block: (Double) -> Unit) {
    var current: Double = from
    while (current < to) {
        block(current)
        current += step
    }
}