package pw.dotdash.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.entity.PlayerInventory
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

val Player.headDirection: Vector3d get() = PlayerUtil.getHeadDirection(this)

val Player.eyeLocation: Location<World> get() = PlayerUtil.getEyeLocation(this)

/**
 * @see [org.spongepowered.api.item.inventory.entity.Hotbar.getSelectedSlotIndex]
 * @see [org.spongepowered.api.item.inventory.entity.Hotbar.setSelectedSlotIndex]
 */
var Player.selectedSlotIndex: Int
    get() = (this.inventory as PlayerInventory).hotbar.selectedSlotIndex
    set(value) {
        (this.inventory as PlayerInventory).hotbar.selectedSlotIndex = value
    }

inline var Player.fallDistance: Float
    get() = this.getOrElse(Keys.FALL_DISTANCE, 0F)
    set(value) {
        this.offer(Keys.FALL_DISTANCE, value)
    }

/**
 * @see [Keys.IS_SNEAKING]
 */
inline var Player.isSneaking: Boolean
    get() = this.getOrElse(Keys.IS_SNEAKING, false)
    set(value) {
        this.offer(Keys.IS_SNEAKING, value)
    }

/**
 * @see [Keys.IS_SPRINTING]
 */
inline var Player.isSprinting: Boolean
    get() = this.getOrElse(Keys.IS_SPRINTING, false)
    set(value) {
        this.offer(Keys.IS_SPRINTING, value)
    }

/**
 * @see [Keys.CAN_FLY]
 */
inline var Player.canFly: Boolean
    get() = this.getOrElse(Keys.CAN_FLY, false)
    set(value) {
        this.offer(Keys.CAN_FLY, value)
    }

/**
 * @see [Keys.IS_FLYING]
 */
inline var Player.isFlying: Boolean
    get() = this.getOrElse(Keys.IS_FLYING, false)
    set(value) {
        this.offer(Keys.IS_FLYING, value)
    }

@JvmOverloads
inline fun Player.getTargetLocation(
    range: Double, checkDiagonals: Boolean = true,
    notSolid: (BlockType) -> Boolean = BlockTypes.AIR::equals
): Location<World> {
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
            location = location.sub(increment)
            break
        }

        i += 0.2
    }

    return location
}