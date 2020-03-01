package pw.dotdash.bending.api.util;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of utilities for dealing with {@link Location}.
 */
public final class LocationUtil {

    public static double distance(Location<World> from, Location<World> to) {
        return checkNotNull(from, "from").getPosition()
                .distance(checkNotNull(to, "to").getPosition());
    }

    public static double distanceSquared(Location<World> from, Location<World> to) {
        return checkNotNull(from, "from").getPosition()
                .distanceSquared(checkNotNull(to, "to").getPosition());
    }

    public static Collection<Location<World>> getNearbyLocations(Location<World> origin, double radius) {
        checkNotNull(origin, "origin");
        final List<Location<World>> result = new ArrayList<>();

        final int originX = origin.getBlockX();
        final int originY = origin.getBlockY();
        final int originZ = origin.getBlockZ();

        final int r = (int) (radius * 4);
        final double radiusSquared = radius * radius;

        for (int x = originX - r; x <= originX + r; x++) {
            for (int y = originY - r; y <= originY + r; y++) {
                for (int z = originZ - r; z <= originZ + r; z++) {
                    final Location<World> location = origin.getExtent().getLocation(x, y, z);
                    if (location.getPosition().distanceSquared(origin.getPosition()) <= radiusSquared) {
                        result.add(location);
                    }
                }
            }
        }

        return result;
    }

    public static boolean isNearDiagonalWall(Location<World> location, Vector3d direction) {
        checkNotNull(location, "location");
        checkNotNull(direction, "direction");
        final boolean isSolidX = BlockTypeUtil.isSolid(location.getBlockRelative(VectorUtil.getDirectionOnAxisX(direction.getX())).getBlockType());
        final boolean isSolidY = BlockTypeUtil.isSolid(location.getBlockRelative(VectorUtil.getDirectionOnAxisY(direction.getX())).getBlockType());
        final boolean isSolidZ = BlockTypeUtil.isSolid(location.getBlockRelative(VectorUtil.getDirectionOnAxisZ(direction.getX())).getBlockType());

        final boolean xz = isSolidX && isSolidZ;
        final boolean xy = isSolidX && isSolidY;
        final boolean yz = isSolidY && isSolidZ;

        return xz || xy || yz;
    }

    public static Location<World> setX(Location<World> location, double x) {
        checkNotNull(location, "location");
        return location.setPosition(new Vector3d(x, location.getY(), location.getZ()));
    }

    public static Location<World> setY(Location<World> location, double y) {
        checkNotNull(location, "location");
        return location.setPosition(new Vector3d(location.getX(), y, location.getZ()));
    }

    public static Location<World> setZ(Location<World> location, double z) {
        checkNotNull(location, "location");
        return location.setPosition(new Vector3d(location.getX(), location.getY(), z));
    }

    private LocationUtil() {
        throw new AssertionError("Don't instantiate me!");
    }
}