package arvenwood.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.property.entity.EyeLocationProperty
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.entity.PlayerInventory
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

inline val Player.eyeLocation: Location<World>
    get() = Location(this.world, this.getProperty(EyeLocationProperty::class.java).get().value!!)

inline val Player.headDirection: Vector3d
    get() = this.headRotation.toDirection()

var Player.selectedSlotIndex: Int
    get() = (this.inventory as PlayerInventory).hotbar.selectedSlotIndex
    set(value) {
        (this.inventory as PlayerInventory).hotbar.selectedSlotIndex = value
    }

@JvmOverloads
inline fun Player.getTargetLocation(range: Double, checkDiagonals: Boolean = true, notSolid: (BlockType) -> Boolean): Location<World> {
    val increment: Vector3d = this.headDirection.normalize().mul(0.2)
    var location: Location<World> = this.eyeLocation

    var i = 0.0
    while (i < range - 1) {
        location = location.add(increment)

        if (checkDiagonals && location.isNearDiagonalWall(increment)) {
            location = location.sub(increment)
            break
        }

        if (!notSolid(location.blockType)) {
            location.sub(increment)
            break
        }

        i += 0.2
    }

    return location
}

@JvmOverloads
fun Player.getTargetLocation(range: Double, checkDiagonals: Boolean = true): Location<World> =
    this.getTargetLocation(range, checkDiagonals, BlockTypes.AIR::equals)