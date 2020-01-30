package arvenwood.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.profile.GameProfile
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

fun Location<World>.getNearbyLocations(radius: Double) : List<Location<World>> {
    val result = ArrayList<Location<World>>()

    val originX = this.blockX
    val originY = this.blockY
    val originZ = this.blockZ

    val r = (radius * 4).toInt()
    val radiusSquared = radius * radius

    for (x in (originX - r)..(originX + r)) {
        for (y in (originY - r)..(originY + r)) {
            for (z in (originZ - r)..(originZ + r)) {
                val location: Location<World> = this.extent.getLocation(x, y, z)
                if (location.position.distanceSquared(this.position) <= radiusSquared) {
                    result += location
                }
            }
        }
    }

    return result
}

fun Location<World>.getNearbyEntities(radius: Double): Collection<Entity> =
    this.extent.getNearbyEntities(this.position, radius)

fun Location<World>.getClosestEntity(radius: Double): Entity? =
    this.getNearbyEntities(radius).minBy { this.distanceSquared(it.location) }

fun Location<World>.isNearDiagonalWall(direction: Vector3d): Boolean {
    val isSolidX: Boolean = this.getBlockRelative(directionOnAxisX(direction.x)).blockType.isSolid()
    val isSolidY: Boolean = this.getBlockRelative(directionOnAxisY(direction.y)).blockType.isSolid()
    val isSolidZ: Boolean = this.getBlockRelative(directionOnAxisZ(direction.z)).blockType.isSolid()

    val xz = isSolidX && isSolidZ
    val xy = isSolidX && isSolidY
    val yz = isSolidY && isSolidZ
    return xz || xy || yz
}

fun Location<World>.distance(other: Location<World>): Double =
    this.position.distance(other.position)

fun Location<World>.distanceSquared(other: Location<World>): Double =
    this.position.distanceSquared(other.position)

fun Location<World>.blockDistance(other: Location<World>): Float =
    this.blockPosition.distance(other.blockPosition)

fun Location<World>.blockDistanceSquared(other: Location<World>): Int =
    this.blockPosition.distanceSquared(other.blockPosition)

fun Location<World>.spawnParticles(particleEffect: ParticleEffect) =
    this.extent.spawnParticles(particleEffect, this.position)

fun Location<World>.spawnParticles(particleEffect: ParticleEffect, radius: Int) =
    this.extent.spawnParticles(particleEffect, this.position, radius)

fun Location<World>.digBlock(profile: GameProfile): Boolean =
    this.extent.digBlock(this.blockPosition, profile)

fun Location<World>.digBlockWith(itemStack: ItemStack, profile: GameProfile): Boolean =
    this.extent.digBlockWith(this.blockPosition, itemStack, profile)

fun Location<World>.add(x: Int, y: Int, z: Int): Location<World> =
    this.add(x.toDouble(), y.toDouble(), z.toDouble())