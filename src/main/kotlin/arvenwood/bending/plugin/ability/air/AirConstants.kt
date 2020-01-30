package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.util.identityHashSetOf
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes

object AirConstants {

    @JvmField
    val DOORS: Set<BlockType> =
        identityHashSetOf(
            BlockTypes.ACACIA_DOOR, BlockTypes.BIRCH_DOOR, BlockTypes.DARK_OAK_DOOR, BlockTypes.IRON_DOOR,
            BlockTypes.JUNGLE_DOOR, BlockTypes.SPRUCE_DOOR, BlockTypes.WOODEN_DOOR
        )

    @JvmField
    val TRAP_DOORS: Set<BlockType> =
        identityHashSetOf(
            BlockTypes.IRON_TRAPDOOR, BlockTypes.TRAPDOOR
        )

    @JvmField
    val BUTTONS: Set<BlockType> =
        identityHashSetOf(
            BlockTypes.STONE_BUTTON, BlockTypes.WOODEN_BUTTON
        )
}