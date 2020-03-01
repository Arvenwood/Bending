package pw.dotdash.bending.api.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.SolidCubeProperty;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of utilities for dealing with {@link BlockType}.
 */
public final class BlockTypeUtil {

    public static final Collection<BlockType> DOORS = ImmutableSet.of(
            BlockTypes.ACACIA_DOOR, BlockTypes.BIRCH_DOOR, BlockTypes.DARK_OAK_DOOR, BlockTypes.IRON_DOOR,
            BlockTypes.JUNGLE_DOOR, BlockTypes.SPRUCE_DOOR, BlockTypes.WOODEN_DOOR
    );

    public static boolean isLiquid(BlockType type) {
        checkNotNull(type, "type");
        return type == BlockTypes.WATER || type == BlockTypes.FLOWING_WATER;
    }

    public static boolean isWater(BlockType type) {
        checkNotNull(type, "type");
        return type == BlockTypes.WATER || type == BlockTypes.FLOWING_WATER;
    }

    public static boolean isSolid(BlockType type) {
        checkNotNull(type, "type");
        final SolidCubeProperty property = type.getProperty(SolidCubeProperty.class).orElse(null);
        if (property == null) {
            return false;
        }
        if (property.getValue() == null) {
            return false;
        }
        return property.getValue();
    }

    private BlockTypeUtil() {
        throw new AssertionError("Don't instantiate me!");
    }
}