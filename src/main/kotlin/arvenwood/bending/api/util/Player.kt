package arvenwood.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.DataTransactionResult
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.property.entity.EyeLocationProperty
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.entity.PlayerInventory
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

inline val Player.eyeLocation: Location<World>
    get() = Location(this.world, this.getProperty(EyeLocationProperty::class.java).get().value!!)

/**
 * Converts the player's [Player.getHeadRotation] into a direction vector.
 *
 * @see [Vector3d.toDirection]
 */
inline val Player.headDirection: Vector3d
    get() = this.headRotation.toDirection()

/**
 * @see [org.spongepowered.api.item.inventory.entity.Hotbar.getSelectedSlotIndex]
 * @see [org.spongepowered.api.item.inventory.entity.Hotbar.setSelectedSlotIndex]
 */
var Player.selectedSlotIndex: Int
    get() = (this.inventory as PlayerInventory).hotbar.selectedSlotIndex
    set(value) {
        (this.inventory as PlayerInventory).hotbar.selectedSlotIndex = value
    }

/**
 * @see [Keys.IS_SNEAKING]
 */
var Player.isSneaking: Boolean
    get() = this.getOrElse(Keys.IS_SNEAKING, false)
    set(value) {
        this.offer(Keys.IS_SNEAKING, value)
    }

/**
 * @see [Keys.IS_SPRINTING]
 */
var Player.isSprinting: Boolean
    get() = this.getOrElse(Keys.IS_SPRINTING, false)
    set(value) {
        this.offer(Keys.IS_SPRINTING, value)
    }

/**
 * @see [Keys.CAN_FLY]
 */
var Player.canFly: Boolean
    get() = this.getOrElse(Keys.CAN_FLY, false)
    set(value) {
        this.offer(Keys.CAN_FLY, value)
    }

/**
 * @see [Keys.IS_FLYING]
 */
var Player.isFlying: Boolean
    get() = this.getOrElse(Keys.IS_FLYING, false)
    set(value) {
        this.offer(Keys.IS_FLYING, value)
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

fun Player.removePotionEffectByType(type: PotionEffectType): DataTransactionResult =
    this.transform(Keys.POTION_EFFECTS) { effects: List<PotionEffect> -> effects.filterNot { it.type == type } }