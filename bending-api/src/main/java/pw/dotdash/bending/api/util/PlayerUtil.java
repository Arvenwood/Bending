package pw.dotdash.bending.api.util;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of utilities for dealing with {@link Player}.
 */
public final class PlayerUtil {

    public static Vector3d getHeadDirection(Player player) {
        return VectorUtil.toDirection(checkNotNull(player, "player").getHeadRotation());
    }

    public static Location<World> getEyeLocation(Player player) {
        checkNotNull(player, "player");
        final Vector3d property = player.getProperty(EyeLocationProperty.class)
                .flatMap(prop -> Optional.ofNullable(prop.getValue()))
                .orElseThrow(() -> new IllegalStateException("Player has no eye location"));
        return new Location<>(player.getWorld(), property);
    }

    public static Location<World> getTargetLocation(Player player, double range, boolean checkDiagonals, Predicate<BlockType> isTransparent) {
        checkNotNull(player, "player");
        checkNotNull(isTransparent, "isTransparent");
        final Vector3d increment = getHeadDirection(player).normalize().mul(0.2);
        Location<World> location = getEyeLocation(player);

        for (double i = 0.0; i < range - 1; i += 0.2) {
            location = location.add(increment);

            if (checkDiagonals && LocationUtil.isNearDiagonalWall(location, increment)) {
                location = location.sub(increment);
            }

            if (!isTransparent.test(location.getBlockType())) {
                location = location.sub(increment);
            }
        }

        return location;
    }

    public static Location<World> getTargetLocation(Player player, double range, boolean checkDiagonals) {
        return getTargetLocation(player, range, checkDiagonals, BlockTypes.AIR::equals);
    }

    private PlayerUtil() {
        throw new AssertionError("Don't instantiate me!");
    }
}