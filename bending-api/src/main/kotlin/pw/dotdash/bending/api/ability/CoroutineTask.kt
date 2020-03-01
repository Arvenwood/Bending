package pw.dotdash.bending.api.ability

import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

@RestrictsSuspension
class CoroutineTask(private val task: AbilityTask) : Continuation<Unit> {

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    suspend fun start(): Unit = suspendCoroutine {
        val task: Task = AbilityService.getInstance().syncExecutor
            .schedule(fun() { it.resume(Unit) }, 0, TimeUnit.MILLISECONDS)
            .task
        this.task.setCurrentTask(task)
    }

    internal fun cleanup() {
        this.task.cancel()
    }

    override fun resumeWith(result: Result<Unit>) {
        cleanup()
        result.getOrThrow()
    }

    suspend fun delay(delay: Long, unit: TimeUnit): Unit = suspendCoroutine {
        val task: Task = AbilityService.getInstance().syncExecutor
            .schedule(fun() { it.resume(Unit) }, delay, unit)
            .task
        this.task.setCurrentTask(task)
    }

    suspend fun delayTicks(ticks: Long): Unit =
        delay(ticks * 50, TimeUnit.MILLISECONDS)

    suspend fun yield(): Unit = delay(0, TimeUnit.MILLISECONDS)
}