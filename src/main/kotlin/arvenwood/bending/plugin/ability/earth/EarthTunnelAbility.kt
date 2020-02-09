package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.SNEAK
import arvenwood.bending.api.ability.AbilityResult.ErrorProtected
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.service.TransientBlockService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.util.BlockStates
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.max

data class EarthTunnelAbility(
    override val cooldown: Long,
    val revertTime: Long,
    val revert: Boolean,
    val dropLootIfNotRevert: Boolean,
    val interval: Long,
    val blocksPerInterval: Int,
    val ignoreOres: Boolean,
    val range: Double,
    val radius: Double,
    val maxRadius: Double
) : Ability<EarthTunnelAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        revertTime = node.getNode("revertTime").long,
        revert = node.getNode("revert").boolean,
        dropLootIfNotRevert = node.getNode("dropLootIfNotRevert").boolean,
        interval = node.getNode("interval").long,
        blocksPerInterval = node.getNode("blocksPerInterval").int,
        ignoreOres = node.getNode("ignoreOres").boolean,
        range = node.getNode("range").double,
        radius = node.getNode("radius").double,
        maxRadius = node.getNode("maxRadius").double
    )

    override val type: AbilityType<EarthTunnelAbility> = AbilityTypes.EARTH_TUNNEL

    override fun prepare(player: Player, context: AbilityContext) {
        context[StandardContext.origin] = player.getTargetLocation(this.range)
        context[StandardContext.direction] = player.headDirection.normalize()
        context[StandardContext.currentLocation] = player.eyeLocation
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context[StandardContext.player] ?: return false
        val location: Location<World> = context[StandardContext.currentLocation] ?: return false
        return !BuildProtectionService.get().isProtected(player, location)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)
        val origin: Location<World> = context.require(StandardContext.origin)
        val direction: Vector3d = context.require(StandardContext.direction)

        var curLoc: Location<World> by context.by(StandardContext.currentLocation)
        var curAngle = 0.0
        var curRadius: Double = this.radius
        var depth: Double = max(0.0, origin.distance(curLoc) - 1)

        abilityLoopUnsafeAt(this.interval) {
            if (!player.isSneaking || player.headDirection.angle(direction).absoluteValue > DEG_20_IN_RAD) {
                return Success
            }

            for (i in 0 until this.blocksPerInterval) {
                while (!(curLoc.blockType.isEarth() || curLoc.blockType.isSand()) || (this.ignoreOres && curLoc.blockType.isOre())) {
                    // TODO check for transparent block
                    if (depth >= this.range) {
                        return Success
                    }

                    if (curAngle >= 360.0) {
                        curAngle = 0.0
                    } else {
                        curAngle += 20.0
                    }

                    if (curRadius >= this.maxRadius) {
                        curRadius = this.radius
                    } else {
                        curRadius += this.radius
                    }

                    depth += 0.5

                    val orthogonal: Vector3d = direction.getOrthogonal(curAngle, curRadius)
                    curLoc = curLoc.add(direction.mul(depth)).add(orthogonal)
                }

                if (BuildProtectionService.get().isProtected(player, curLoc)) {
                    return ErrorProtected
                }

                when {
                    this.revert -> {
                        TransientBlockService.get().createSnapshotBuilder()
                            .location(curLoc)
                            .newState(BlockStates.AIR)
                            .delay(this.revertTime, TimeUnit.MILLISECONDS)
                            .submit()
                    }
                    this.dropLootIfNotRevert -> {
                        curLoc.digBlock(player.profile)
                    }
                    else -> {
                        curLoc.blockType = BlockTypes.AIR
                    }
                }
            }
        }
    }

    companion object {
        private const val DEG_20_IN_RAD: Double = 0.3490658504
    }
}