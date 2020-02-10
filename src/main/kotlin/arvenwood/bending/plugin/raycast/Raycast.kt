package arvenwood.bending.plugin.raycast

import arvenwood.bending.api.ability.AbilityResult
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.util.*
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

data class Raycast(
    @JvmField
    val origin: Location<World>,
    @JvmField
    val direction: Vector3d,
    @JvmField
    val range: Double,
    @JvmField
    val speed: Double,
    @JvmField
    val checkDiagonals: Boolean
) {

    @JvmField
    val speedFactor: Double = this.speed * (50 / 1000.0)

    @JvmField
    val rangeSquared: Double = this.range * this.range

    @JvmField
    var location: Location<World> = this.origin

    inline fun advance(block: Raycast.(Location<World>) -> AbilityResult): AbilityResult {
        if (this.range > 0 && this.location.distanceSquared(this.origin) > this.rangeSquared) {
            return ErrorOutOfRange
        }

        if (this.checkDiagonals && this.location.isNearDiagonalWall(this.direction)) {
            return ErrorWallReached
        }

        val result: AbilityResult = this.block(this.location)
        if (result != Success) {
            return result
        }

        this.location = this.location.next()

        return Success
    }

    fun Location<World>.next(): Location<World> =
        this + direction * speedFactor

    inline fun affectLocations(
        source: Player, affected: MutableCollection<Location<World>>,
        radius: Double, affect: (test: Location<World>) -> Boolean
    ) {
        for (test: Location<World> in this.location.getNearbyLocations(radius)) {
            if (test in affected) {
                continue
            }

            if (BuildProtectionService.get().isProtected(source, test)) {
                // Can't fight here!
                continue
            }

            if (affect(test)) {
                affected += test
            }
        }
    }

    inline fun affectEntities(
        source: Player, affected: MutableCollection<Entity>,
        radius: Double, affect: (test: Entity) -> Boolean
    ) {
        for (test: Entity in this.location.getNearbyEntities(radius)) {
            if (test in affected) {
                continue
            }

            if (PvpProtectionService.get().isProtected(source, test)) {
                // Can't fight here!
                continue
            }

            if (affect(test)) {
                affected += test
            }
        }
    }

    @JvmOverloads
    fun damageEntity(test: Entity, damage: Double, source: DamageSource = DamageSources.MAGIC): Boolean {
        if (damage <= 0 || test !is Living) return false

        test.damage(damage, source)
        return true
    }

    fun playParticles(effect: ParticleEffect) {
        this.location.spawnParticles(effect)
    }

    fun playSounds(sound: SoundType, volume: Double, pitch: Double) {
        this.location.extent.playSound(sound, this.location.position, volume, pitch)
    }

    fun playSounds(sound: SoundType, volume: Double, pitch: Double, minVolume: Double) {
        this.location.extent.playSound(sound, this.location.position, volume, pitch, minVolume)
    }
}

/**
 * @return Whether any of the rays succeeded
 */
inline fun Iterable<Raycast>.advanceAll(block: Raycast.(location: Location<World>) -> AbilityResult): Boolean {
    var successful = false
    for (projectile: Raycast in this) {
        val result: AbilityResult = projectile.advance { location: Location<World> ->
            this.block(location)
        }

        if (result == Success) {
            successful = true
        }
    }
    return successful
}