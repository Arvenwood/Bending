package pw.dotdash.bending.plugin.util

import org.spongepowered.api.block.BlockState
import org.spongepowered.api.block.BlockTypes

object BlockStates {

    @JvmField
    val AIR: BlockState = BlockState.builder().blockType(BlockTypes.AIR).build()
}