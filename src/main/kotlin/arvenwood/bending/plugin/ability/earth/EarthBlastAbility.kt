package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.util.enumSetOf
import ninja.leaping.configurate.ConfigurationNode

data class EarthBlastAbility(
    override val cooldown: Long,
    val collisionRadius: Double,
    val damage: Double,
    val deflectRange: Double,
    val pushFactor: Double,
    val range: Double,
    val selectRange: Double,
    val speed: Double,
    val canHitSelf: Boolean
) : Ability<EarthBlastAbility> {

    override val type: AbilityType<EarthBlastAbility> = EarthBlastAbility

    companion object : AbstractAbilityType<EarthBlastAbility>(
        element = Elements.Earth,
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:earth_blast",
        name = "EarthBlast"
    ) {
        override val default: Ability<EarthBlastAbility>
            get() = TODO("not implemented")

        override fun load(node: ConfigurationNode): EarthBlastAbility {
            TODO("not implemented")
        }
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        TODO("not implemented")
    }
}