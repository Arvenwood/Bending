package pw.dotdash.bending.api.util

import com.flowpowered.math.vector.Vector3d
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

fun Vector3d.angle(other: Vector3d): Double {
    return acos(dot(other) / (length() * other.length()))
}

fun Vector3d.setX(x: Double): Vector3d =
    VectorUtil.setX(this, x)

fun Vector3d.setY(y: Double): Vector3d =
    VectorUtil.setY(this, y)

fun Vector3d.setZ(z: Double): Vector3d =
    VectorUtil.setZ(this, z)

/**
 * Gets the orthogonal vector of the given axis vector.
 *
 * @receiver The axis vector
 */
fun Vector3d.getOrthogonal(degrees: Double, length: Double): Vector3d {
    val orthogonal = Vector3d(y, -x, 0.0).normalize().mul(length)
    return this.rotateAround(orthogonal, degrees)
}

fun Vector3d.getOrthogonal(direction: Vector3d): Vector3d =
    this.getOrthogonal(Math.toDegrees(direction.angle(this)), 1.0)

fun Vector3d.rotateAround(other: Vector3d, degrees: Double): Vector3d {
    val angle: Double = Math.toRadians(degrees)
    val thirdAxis: Vector3d = this.cross(other).normalize().mul(other.length())
    return other.mul(cos(angle)).add(thirdAxis.mul(sin(angle)))
}

fun Vector3d.rotateAroundAxis(direction: Vector3d): Vector3d =
    this.rotateAroundAxisX(direction.x).rotateAroundAxisY(-direction.y).rotateAroundAxisZ(direction.z)

fun Vector3d.rotateAroundAxisX(angle: Double): Vector3d {
    val sin: Double = sin(angle)
    val cos: Double = cos(angle)
    return Vector3d(
        this.x,
        this.y * cos - this.z * sin,
        this.y * sin + this.z * cos
    )
}

fun Vector3d.rotateAroundAxisY(angle: Double): Vector3d {
    val sin: Double = sin(angle)
    val cos: Double = cos(angle)
    return Vector3d(
        this.x * cos + this.z * sin,
        this.y,
        this.x * -sin + this.z * cos
    )
}

fun Vector3d.rotateAroundAxisZ(angle: Double): Vector3d {
    val sin: Double = sin(angle)
    val cos: Double = cos(angle)
    return Vector3d(
        this.x * cos - this.y * sin,
        this.x * sin + this.y * cos,
        this.z
    )
}