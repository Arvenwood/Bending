package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorProtected
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.service.TransientBlockService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
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

    override val type: AbilityType<EarthTunnelAbility> = EarthTunnelAbility

    companion object : AbstractAbilityType<EarthTunnelAbility>(
        element = Elements.Earth,
        executionTypes = enumSetOf(AbilityExecutionType.SNEAK),
        id = "bending:earth_tunnel",
        name = "EarthTunnel"
    ) {
        override val default: Ability<EarthTunnelAbility>
            get() = TODO("not implemented")

        override fun load(node: ConfigurationNode): EarthTunnelAbility {
            TODO("not implemented")
        }

        private const val DEG_20_IN_RAD: Double = 0.3490658504
    }

    override fun prepare(player: Player, context: AbilityContext) {
        context[StandardContext.origin] = player.getTargetLocation(this.range)
        context[StandardContext.direction] = player.headDirection.normalize()
        context[StandardContext.currentLocation] = player.eyeLocation
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)
        val origin: Location<World> = context.require(StandardContext.origin)
        val direction: Vector3d = context.require(StandardContext.direction)

        var curLoc: Location<World> by context.by(StandardContext.currentLocation)
        var curAngle = 0.0
        var curRadius: Double = this.radius
        var depth: Double = max(0.0, origin.distance(curLoc) - 1)

        var curTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (curTime + this.interval >= System.currentTimeMillis()) {
                // We must wait a bit longer.
                return@abilityLoopUnsafe
            }

            curTime = System.currentTimeMillis()

            if (player.getOrElse(Keys.IS_SNEAKING, false) || player.headDirection.angle(direction).absoluteValue > DEG_20_IN_RAD) {
                return Success
            }

            for (i in 0 until this.blocksPerInterval) {
                while ((!(curLoc.blockType.isEarth() || curLoc.blockType.isSand())) || (this.ignoreOres && curLoc.blockType.isOre())) {
                    // TODO check for transparent block
                    if (depth >= this.range) {
                        return Success
                    }

                    if (curAngle >= 360) {
                        curAngle = 0.0
                    } else {
                        curAngle += 20
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
}