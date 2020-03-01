package pw.dotdash.bending.api.ability

import org.spongepowered.api.plugin.PluginContainer
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

@RestrictsSuspension
class CoroutineTask(private val task: AbilityTask, private val plugin: PluginContainer) : Continuation<Unit> {

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    private var scheduler: TaskScheduler = DelayedTaskScheduler(this.task, this.plugin)

    suspend fun start(): Unit = suspendCoroutine {
        this.scheduler.forceNewTask { it.resume(Unit) }
    }

    internal fun cleanup() {
        this.task.cancel()
    }

    override fun resumeWith(result: Result<Unit>) {
        cleanup()
        result.getOrThrow()
    }

    suspend fun delay(delay: Long, unit: TimeUnit): Unit = suspendCoroutine {
        this.scheduler.delay(delay, unit, fun() { it.resume(Unit) })
    }

    suspend fun delayTicks(ticks: Long): Unit =
        delay(ticks * 50, TimeUnit.MILLISECONDS)

    suspend fun yield(): Unit = suspendCoroutine {
        this.scheduler.yield(fun() { it.resume(Unit) })
    }

    suspend fun repeating(interval: Long, unit: TimeUnit): Unit = suspendCoroutine {
        this.scheduler = RepeatingTaskScheduler(this.task, this.plugin, interval, unit)
        this.scheduler.forceNewTask { it.resume(Unit) }
    }

    suspend fun nonRepeating(): Unit = suspendCoroutine {
        this.scheduler = DelayedTaskScheduler(this.task, this.plugin)
        this.scheduler.forceNewTask { it.resume(Unit) }
    }

    /**
     * Loops until the ability finishes, or until the ability loops [MAX_TICKS] times.
     */
    suspend inline fun abilityLoop(block: () -> Unit) {
        repeating(AbilityService.getInstance().abilityDelayMilli, TimeUnit.MILLISECONDS)
        for (tick: Int in 0 until MAX_TICKS) {
            block()
            yield()
        }
        nonRepeating()
    }

    /**
     * Loops indefinitely until the block returns from the parent function or throws an exception.
     * Use with care, otherwise this can cause the main thread to halt.
     */
    suspend inline fun abilityLoopUnsafe(block: () -> Unit): Nothing {
        repeating(AbilityService.getInstance().abilityDelayMilli, TimeUnit.MILLISECONDS)
        while (true) {
            block()
            yield()
        }
    }

    /**
     * Loops indefinitely until the block returns from the parent function or throws an exception.
     * Use with care, otherwise this can cause the main thread to halt.
     */
    suspend inline fun abilityLoopUnsafeAt(intervalMilli: Long, block: () -> Unit): Nothing {
        repeating(intervalMilli, TimeUnit.MILLISECONDS)
        while (true) {
            block()
            yield()
        }
    }

    companion object {
        @PublishedApi
        internal const val MAX_TICKS: Int = 10000
    }
}