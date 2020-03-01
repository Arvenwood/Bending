package pw.dotdash.bending.api.ability

import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit

internal interface TaskScheduler {

    val task: AbilityTask

    val plugin: PluginContainer

    fun delay(delay: Long, unit: TimeUnit, block: () -> Unit)

    fun yield(block: () -> Unit)

    fun forceNewTask(block: () -> Unit)
}

internal class DelayedTaskScheduler(override val task: AbilityTask, override val plugin: PluginContainer) : TaskScheduler {

    override fun delay(delay: Long, unit: TimeUnit, block: () -> Unit) {
        val task: Task = Task.builder()
            .delay(delay, unit)
            .execute(block)
            .submit(this.plugin)
        this.task.setCurrentTask(task)
    }

    override fun yield(block: () -> Unit) {
        forceNewTask(block)
    }

    override fun forceNewTask(block: () -> Unit) {
        val task: Task = Task.builder()
            .execute(block)
            .submit(this.plugin)
        this.task.setCurrentTask(task)
    }
}

internal class RepeatingTaskScheduler(
    override val task: AbilityTask, override val plugin: PluginContainer,
    private val interval: Long, private val unit: TimeUnit
) : TaskScheduler {

    private var next: RepeatingContinuation? = null

    override fun delay(delay: Long, unit: TimeUnit, block: () -> Unit) {
        this.next = RepeatingContinuation(block, this.unit.convert(delay, unit))
    }

    override fun yield(block: () -> Unit) {
        this.next = RepeatingContinuation(block, 0)
    }

    override fun forceNewTask(block: () -> Unit) {
        yield(block)
        val task: Task = Task.builder()
            .interval(this.interval, this.unit)
            .execute(fun() { this.next?.resume(this.interval) })
            .submit(this.plugin)
        this.task.setCurrentTask(task)
    }

    private class RepeatingContinuation(val block: () -> Unit, val delay: Long) {

        private var passedTime: Long = 0L
        private var resumed: Boolean = false

        fun resume(passedTime: Long) {
            check(!this.resumed) { "Already resumed" }
            this.passedTime += passedTime
            if (this.passedTime >= this.delay) {
                this.resumed = true
                this.block()
            }
        }
    }
}