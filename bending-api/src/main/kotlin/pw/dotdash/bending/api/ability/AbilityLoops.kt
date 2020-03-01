package pw.dotdash.bending.api.ability

import kotlinx.coroutines.delay

@PublishedApi
internal const val MAX_TICKS: Int = 10000

/**
 * Loops until the ability finishes, or until the ability loops [MAX_TICKS] times.
 */
suspend inline fun CoroutineTask.abilityLoop(block: () -> Unit) {
    val delayTicks: Long = AbilityService.getInstance().abilityDelayMilli / 50
    for (tick: Int in 0 until MAX_TICKS) {
        block()
        delayTicks(delayTicks)
    }
}

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun CoroutineTask.abilityLoopUnsafe(block: () -> Unit): Nothing {
    val delayTicks: Long = AbilityService.getInstance().abilityDelayMilli / 50
    while (true) {
        block()
        delayTicks(delayTicks)
    }
}

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun CoroutineTask.abilityLoopUnsafeAt(intervalMilli: Long, block: () -> Unit): Nothing {
    while (true) {
        block()
        delayTicks(intervalMilli)
    }
}

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun abilityLoopUnsafe(block: () -> Unit): Nothing {
    val delayTicks: Long = AbilityService.getInstance().abilityDelayMilli / 50
    while (true) {
        block()
        delay(delayTicks)
    }
}

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun abilityLoopUnsafeAt(intervalMilli: Long, block: () -> Unit): Nothing {
    while (true) {
        block()
        delay(intervalMilli)
    }
}

suspend inline fun abilityLoop(block: () -> Unit) {
    val delayTicks: Long = AbilityService.getInstance().abilityDelayMilli / 50
    for (tick: Int in 0 until MAX_TICKS) {
        block()
        delay(delayTicks)
    }
}