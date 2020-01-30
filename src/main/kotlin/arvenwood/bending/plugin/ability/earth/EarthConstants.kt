package arvenwood.bending.plugin.ability.earth

import arvenwood.bending.api.util.identityHashSetOf
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.block.BlockTypes.*

object EarthConstants {

    @JvmField
    val EARTH: Set<BlockType> = identityHashSetOf(
        GRASS, GRASS_PATH,
        DIRT, MYCELIUM, GRAVEL, CLAY,
        STONE, STONE_SLAB,
        COAL_ORE, REDSTONE_ORE, LAPIS_ORE, DIAMOND_ORE, EMERALD_ORE,
        COBBLESTONE, COBBLESTONE_WALL, NETHERRACK
    )

    @JvmField
    val SAND: Set<BlockType> = identityHashSetOf(
        BlockTypes.SAND, SANDSTONE, SANDSTONE_STAIRS,
        RED_SANDSTONE, RED_SANDSTONE_STAIRS
    )

    @JvmField
    val ORES: Set<BlockType> = identityHashSetOf(
        COAL_ORE, IRON_ORE, GOLD_ORE, LAPIS_ORE,
        QUARTZ_ORE, DIAMOND_ORE, EMERALD_ORE,
        REDSTONE_ORE, LIT_REDSTONE_ORE
    )
}

fun BlockType.isEarth(): Boolean = this in EarthConstants.EARTH

fun BlockType.isSand(): Boolean = this in EarthConstants.SAND

fun BlockType.isOre(): Boolean = this in EarthConstants.ORES