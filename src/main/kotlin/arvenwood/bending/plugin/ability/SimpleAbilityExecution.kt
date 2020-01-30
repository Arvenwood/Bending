package arvenwood.bending.plugin.ability

import arvenwood.bending.api.ability.*
import kotlinx.coroutines.Job

data class SimpleAbilityExecution(val job: Job) : AbilityExecution {

    override val ability: Ability<*> get() = this.job[Ability]!!

    override val type: AbilityType<*> get() = ability.type

    override val context: AbilityContext get() = this.job[AbilityContext]!!

    override val executionType: AbilityExecutionType get() = this.job[AbilityExecutionType]!!

    override fun cancel() {
        this.job.cancel()
    }
}