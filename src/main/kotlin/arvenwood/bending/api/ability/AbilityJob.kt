package arvenwood.bending.api.ability

interface AbilityJob {

    val ability: Ability<*>

    val type: AbilityType<*>

    val context: AbilityContext

    val executionType: AbilityExecutionType

    fun cancel()
}