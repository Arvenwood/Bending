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
    val orthogonal: Vector3d = Vector3d(y, -x, 0.0).normalize().mul(length)
    val angle: Double = Math.toRadians(degrees)
    val thirdAxis: Vector3d = this.cross(orthogonal).normalize().mul(orthogonal.length())
    return orthogonal.mul(cos(angle)).add(thirdAxis.mul(sin(angle)))
}