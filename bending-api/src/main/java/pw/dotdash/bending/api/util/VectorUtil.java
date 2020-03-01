package pw.dotdash.bending.api.util;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.util.Direction;

/**
 * A collection of utilities for dealing with {@link Vector3d}.
 */
public final class VectorUtil {

    /**
     * A (0.275, 0.275, 0.275) 3D vector.
     */
    public static final Vector3d VECTOR_0_275 = new Vector3d(0.275, 0.275, 0.275);

    /**
     * A (0.2, 0.2, 0.2) 3D vector.
     */
    public static final Vector3d VECTOR_0_2 = new Vector3d(0.2, 0.2, 0.2);

    /**
     * A (0.4, 0.4, 0.4) 3D vector.
     */
    public static final Vector3d VECTOR_0_4 = new Vector3d(0.4, 0.4, 0.4);

    public static double getAngleBetween(Vector3d first, Vector3d second) {
        return Math.acos(first.dot(second) / (first.length() * second.length()));
    }

    public static Vector3d toDirection(Vector3d rotation) {
        return Quaterniond.fromAxesAnglesDeg(rotation.getX(), -rotation.getY(), rotation.getZ()).getDirection();
    }

    public static Direction getDirectionOnAxisX(double length) {
        if (length > 0) {
            return Direction.EAST;
        } else if (length < 0) {
            return Direction.WEST;
        } else {
            return Direction.NONE;
        }
    }

    public static Direction getDirectionOnAxisY(double length) {
        if (length > 0) {
            return Direction.UP;
        } else if (length < 0) {
            return Direction.DOWN;
        } else {
            return Direction.NONE;
        }
    }

    public static Direction getDirectionOnAxisZ(double length) {
        if (length > 0) {
            return Direction.SOUTH;
        } else if (length < 0) {
            return Direction.NORTH;
        } else {
            return Direction.NONE;
        }
    }

    public static Vector3d setX(Vector3d v, double x) {
        return new Vector3d(x, v.getY(), v.getZ());
    }

    public static Vector3d setY(Vector3d v, double y) {
        return new Vector3d(v.getX(), y, v.getZ());
    }

    public static Vector3d setZ(Vector3d v, double z) {
        return new Vector3d(v.getX(), v.getY(), z);
    }

    private VectorUtil() {
        throw new AssertionError("Don't instantiate me!");
    }
}