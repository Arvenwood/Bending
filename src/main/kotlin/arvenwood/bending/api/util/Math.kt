package arvenwood.bending.api.util

import com.flowpowered.math.imaginary.Quaterniond
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.util.Direction
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Converts a rotation vector to a direction vector.
 */
fun Vector3d.toDirection(): Vector3d =
    Quaterniond.fromAxesAnglesDeg(x, -y, z).direction

fun Vector3d.withX(x: Double): Vector3d =
    Vector3d(x, this.y, this.z)

fun Vector3d.withY(y: Double): Vector3d =
    Vector3d(this.x, y, this.z)

fun Vector3d.withZ(z: Double): Vector3d =
    Vector3d(this.x, this.y, z)

fun Vector3d.withXZ(x: Double, y: Double): Vector3d =
    Vector3d(x, this.y, this.z)

fun Vector3d.angle(other: Vector3d): Double =
    acos(this.dot(other) / (this.length() * other.length()))

/**
 * Gets the [Direction] from the given vector axis and length.
 *
 * @param axis The axis, 0 for x, 1 for y, 2 for z
 * @param length The vector length
 * @return The direction of the single dimension vector
 */
fun directionOnAxis(axis: Int, length: Double): Direction? =
    when (axis) {
        0 -> directionOnAxisX(length)
        1 -> directionOnAxisY(length)
        2 -> directionOnAxisZ(length)
        else -> null
    }

fun directionOnAxisX(length: Double): Direction =
    when {
        length > 0 -> Direction.EAST
        length < 0 -> Direction.WEST
        else -> Direction.NONE
    }

fun directionOnAxisY(length: Double): Direction =
    when {
        length > 0 -> Direction.UP
        length < 0 -> Direction.DOWN
        else -> Direction.NONE
    }

fun directionOnAxisZ(length: Double): Direction =
    when {
        length > 0 -> Direction.SOUTH
        length < 0 -> Direction.NORTH
        else -> Direction.NONE
    }

/**
 * Gets the orthogonal vector of the given axis vector.
 *
 * @receiver The axis vector
 */
fun Vector3d.getOrthogonal(degrees: Double, length: Double): Vector3d {
    val orthogonal = Vector3d(y, -x, 0.0).normalize().mul(length)
    return this.rotateAround(orthogonal, degrees)
}

fun Vector3d.rotateAround(other: Vector3d, degrees: Double): Vector3d {
    val angle: Double = Math.toRadians(degrees)
    val thirdAxis: Vector3d = this.cross(other).normalize().mul(other.length())
    return other.mul(cos(angle)).add(thirdAxis.mul(sin(angle)))
}