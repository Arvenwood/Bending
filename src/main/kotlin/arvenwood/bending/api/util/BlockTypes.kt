package arvenwood.bending.api.util

import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.property.block.SolidCubeProperty

fun BlockType.isLiquid(): Boolean =
    this == BlockTypes.WATER || this == BlockTypes.FLOWING_WATER

fun BlockType.isWater(): Boolean =
    this == BlockTypes.WATER || this == BlockTypes.FLOWING_WATER

fun BlockType.isSolid(): Boolean =
    this.property<SolidCubeProperty>()?.value == true