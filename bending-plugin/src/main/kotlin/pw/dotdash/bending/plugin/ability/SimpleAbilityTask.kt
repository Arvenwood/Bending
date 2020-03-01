package pw.dotdash.bending.plugin.ability

import org.spongepowered.api.scheduler.Task
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.AbilityTask
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.plugin.bender.SimpleBender
import java.util.*

data class SimpleAbilityTask(
    private val bender: SimpleBender,
    private val ability: Ability,
    private val type: AbilityType,
    private val context: AbilityContext,
    private val executionType: AbilityExecutionType
) : AbilityTask {

    private var task: Task? = null

    override fun getBender(): SimpleBender = this.bender

    override fun getAbility(): Ability = this.ability

    override fun getType(): AbilityType = this.type

    override fun getContext(): AbilityContext = this.context

    override fun getExecutionType(): AbilityExecutionType = this.executionType

    override fun getCurrentTask(): Optional<Task> =
        Optional.ofNullable(this.task)

    override fun setCurrentTask(task: Task?) {
        this.task?.cancel()
        this.task = task
    }

    override fun cancel(): Boolean {
        val result: Boolean = this.task?.cancel() ?: false
        this.task = null
        if (result) {
            this.ability.cleanup(this.context)
            this.bender.cancelAbility(this)
        }
        return result
    }
}