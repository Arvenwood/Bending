package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.util.identityHashSetOf
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes

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

    @JvmField
    val VECTOR_0_275: Vector3d = Vector3d(0.275, 0.275, 0.275)

    @JvmField
    val VECTOR_0_2: Vector3d = Vector3d(0.2, 0.2, 0.2)

    @JvmField
    val EXTINGUISH_EFFECT: ParticleEffect = ParticleEffect.builder().type(ParticleTypes.FIRE_SMOKE).build()
}