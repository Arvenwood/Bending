package pw.dotdash.bending.api.ability

import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.plugin.PluginContainer
import kotlin.coroutines.startCoroutine

abstract class CoroutineAbility(open val cooldownMilli: Long, private val type: AbilityType) : Ability {

    abstract val plugin: PluginContainer

    override fun getCooldown(): Long = this.cooldownMilli

    override fun getType(): AbilityType = this.type

    override fun prepare(cause: Cause, context: AbilityContext) {}

    override fun validate(context: AbilityContext): Boolean = true

    override fun preempt(context: AbilityContext, executionType: AbilityExecutionType) {}

    abstract suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType)

    override fun execute(context: AbilityContext, executionType: AbilityExecutionType, task: AbilityTask) {
        val coroutine = CoroutineTask(task, this.plugin)
        val block: suspend CoroutineTask.() -> Unit = {
            try {
                this.start()
                this.activate(context, executionType)
            } finally {
                this.cleanup()
            }
        }
        block.startCoroutine(coroutine, coroutine)
    }

    override fun cleanup(context: AbilityContext) {}
}