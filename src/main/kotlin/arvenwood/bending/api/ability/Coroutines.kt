package arvenwood.bending.api.ability

import arvenwood.bending.api.service.AbilityService
import kotlinx.coroutines.delay
import org.spongepowered.api.entity.living.player.Player

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun abilityLoopUnsafe(block: () -> Unit): Nothing {
    val delayMilli: Long = AbilityService.get().abilityDelayMilli
    while (true) {
        block()
        delay(delayMilli)
    }
}

/**
 * Loops indefinitely until the block returns from the parent function or throws an exception.
 * Use with care, otherwise this can cause the main thread to halt.
 */
suspend inline fun abilityLoopUnsafeQuarterTime(block: () -> Unit): Nothing {
    val delayMilli: Long = AbilityService.get().abilityDelayMilli * 4
    while (true) {
        block()
        delay(delayMilli)
    }
}

@PublishedApi
internal const val MAX_TICKS: Int = 10000

suspend inline fun abilityLoop(block: () -> Unit) {
    val delayMilli: Long = AbilityService.get().abilityDelayMilli
    for (tick: Int in 0 until MAX_TICKS) {
        block()
        delay(delayMilli)
    }
}