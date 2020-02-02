package arvenwood.bending.plugin.projectile

import arvenwood.bending.api.ability.AbilityResult
import arvenwood.bending.api.util.distanceSquared
import arvenwood.bending.api.util.isNearDiagonalWall
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

data class FireProjectile(
    val origin: Location<World>,
    val direction: Vector3d,
    val range: Double,
    val speed: Double,
    val checkDiagonals: Boolean
) {

    val speedFactor: Double = this.speed * (50 / 1000.0)
    val rangeSquared: Double = this.range * this.range

    var location: Location<World> = this.origin

    inline fun advance(block: (Location<World>) -> Unit): AbilityResult {
        if (this.range > 0 && this.location.distanceSquared(this.origin) > this.rangeSquared) {
            // Range limit reached!
            return AbilityResult.ErrorOutOfRange
        }

        if (this.checkDiagonals && this.location.isNearDiagonalWall(this.direction)) {
            // Stop if we've hit a diagonal wall.
            return AbilityResult.ErrorWallReached
        }

        val next: Location<World> = this.location.add(this.direction.mul(this.speedFactor))

        block(next)

        // Move forward.
        this.location = next

        return AbilityResult.Success
    }
}

inline fun Iterable<FireProjectile>.advanceAll(block: (projectile: FireProjectile, location: Location<World>) -> Unit): Boolean {
    var successful = false
    for (projectile: FireProjectile in this) {
        val result: AbilityResult = projectile.advance { location: Location<World> ->
            block(projectile, location)
        }

        if (result == AbilityResult.Success) {
            successful = true
        }
    }
    return successful
}