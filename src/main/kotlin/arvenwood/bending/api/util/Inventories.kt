package arvenwood.bending.api.util

import org.spongepowered.api.item.inventory.Slot
import org.spongepowered.api.item.inventory.property.SlotIndex

/**
 * Fetches the index of this slot.
 */
val Slot.index: Int
    get() = this.getInventoryProperty(SlotIndex::class.java).map { it.value!! }.orElse(0)