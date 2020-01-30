package arvenwood.bending.api.util.async

import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Scheduler
import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@UseExperimental(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class SpongeDispatcher(
    private val plugin: Any,
    private val async: Boolean
) : CoroutineDispatcher(), Delay {

    private val scheduler: Scheduler = Sponge.getScheduler()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) return

        if (!this.async && Sponge.getServer().isMainThread) {
            block.run()
        } else {
            this.scheduler.createTaskBuilder()
                .also { if (this.async) it.async() }
                .execute(block)
                .submit(this.plugin)
        }
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        @Suppress("RedundantLambdaArrow")
        val task: Task = this.scheduler.createTaskBuilder()
            .also { if (this.async) it.async() }
            .execute { -> continuation.apply { resumeUndispatched(Unit) } }
            .delay(timeMillis, TimeUnit.MILLISECONDS)
            .submit(this.plugin)

        continuation.invokeOnCancellation { task.cancel() }
    }
}