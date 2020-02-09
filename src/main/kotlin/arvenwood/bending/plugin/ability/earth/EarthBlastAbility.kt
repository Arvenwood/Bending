package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityExecutionTypes.SNEAK
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.util.getTargetLocation
import arvenwood.bending.plugin.ability.AbilityTypes
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
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
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        collisionRadius = node.getNode("collisionRadius").double,
        damage = node.getNode("damage").double,
        deflectRange = node.getNode("deflectRange").double,
        pushFactor = node.getNode("pushFactor").double,
        range = node.getNode("range").double,
        selectRange = node.getNode("selectRange").double,
        speed = node.getNode("speed").double,
        canHitSelf = node.getNode("canHitSelf").boolean
    )

    override val type: AbilityType<EarthBlastAbility> = AbilityTypes.EARTH_BLAST

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

        bender.awaitExecution(this.type, LEFT_CLICK)

        var isAtDestination = false
        var isProgressing = false
        var location: Location<World> = sourceLocation

        abilityLoopUnsafeAt(this.interval) {
            if (player.isRemoved) return ErrorDied
            if (isAtDestination || !isProgressing) return Success
        }
    }
}