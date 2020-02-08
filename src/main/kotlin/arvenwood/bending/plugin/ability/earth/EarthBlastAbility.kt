package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityExecutionType.SNEAK
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.util.enumSetOf
import arvenwood.bending.api.util.getTargetLocation
import com.google.common.base.Predicates
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

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
        executionTypes = enumSetOf(LEFT_CLICK, SNEAK),
        id = "bending:earth_blast",
        name = "EarthBlast"
    ) {
        override fun load(node: ConfigurationNode): EarthBlastAbility {
            TODO("not implemented")
        }
    }

    private val interval: Long = (1000.0 / this.speed).toLong()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        if (executionType != SNEAK) {
            // Only allow normal activation through sneaking.
            return Success
        }

        val player: Player = context.require(StandardContext.player)
        val bender: Bender = context.require(StandardContext.bender)

        val sourceLocation: Location<World> = player.getTargetLocation(this.selectRange)
            .takeIf { it.blockType.isEarth() && !BuildProtectionService.get().isProtected(player, it) } ?: return ErrorNoTarget

        val sourceType: BlockType = sourceLocation.blockType
        sourceLocation.blockType = when (sourceType) {
            BlockTypes.SAND -> BlockTypes.SANDSTONE
            BlockTypes.STONE -> BlockTypes.COBBLESTONE
            else -> BlockTypes.STONE
        }

        bender.awaitExecution(EarthBlastAbility, LEFT_CLICK)

        var isAtDestination = false
        var isProgressing = false
        var location: Location<World> = sourceLocation

        abilityLoopUnsafeAt(this.interval) {
            if (player.isRemoved) return ErrorDied
            if (isAtDestination || !isProgressing) return Success
        }
    }
}