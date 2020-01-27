package arvenwood.bending.api.ability

interface AbilityExecution {

    val ability: Ability<*>

    val type: AbilityType<*>

    val context: AbilityContext

    val executionType: AbilityExecutionType

    fun cancel()
}