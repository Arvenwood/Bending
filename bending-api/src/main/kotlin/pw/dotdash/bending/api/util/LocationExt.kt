package pw.dotdash.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

fun Location<World>.distance(to: Location<World>): Double {
    return this.position.distance(to.position)
}

fun Location<World>.distanceSquared(to: Location<World>): Double {
    return this.position.distanceSquared(to.position)
}

fun Location<World>.isNearDiagonalWall(direction: Vector3d): Boolean =
    LocationUtil.isNearDiagonalWall(this, direction)

fun Location<World>.spawnParticles(particleEffect: ParticleEffect) =
    this.extent.spawnParticles(particleEffect, this.position)

fun Location<World>.spawnParticles(particleEffect: ParticleEffect, radius: Int) =
    this.extent.spawnParticles(particleEffect, this.position, radius)

fun Location<World>.getNearbyLocations(radius: Double): Collection<Location<World>> =
    LocationUtil.getNearbyLocations(this, radius)

fun Location<World>.getNearbyEntities(radius: Double): Collection<Entity> =
    this.extent.getNearbyEntities(this.position, radius)

fun Location<World>.setX(x: Double): Location<World> =
    LocationUtil.setX(this, x)

fun Location<World>.setY(y: Double): Location<World> =
    LocationUtil.setY(this, y)

fun Location<World>.setZ(z: Double): Location<World> =
    LocationUtil.setZ(this, z)