package pw.dotdash.bending.api.ray

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
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.util.*
import java.util.function.BiPredicate

/**
 * An optimized version of [Raycast] that utilizes Kotlin inline methods.
 */
data class FastRaycast(
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
) : Raycast {

    @JvmField
    val speedFactor: Double = this.speed * (50 / 1000.0)

    @JvmField
    val rangeSquared: Double = this.range * this.range

    @JvmField
    var location: Location<World> = this.origin

    override fun getOrigin(): Location<World> = this.origin

    override fun getDirection(): Vector3d = this.direction

    override fun getRange(): Double = this.range

    override fun getSpeed(): Double = this.speed

    override fun getSpeedFactor(): Double = this.speedFactor

    override fun isDiagonalChecked(): Boolean = this.checkDiagonals

    override fun advance(block: BiPredicate<Raycast, Location<World>>): Boolean =
        this.progress {
            block.test(this, it)
        }

    inline fun progress(block: FastRaycast.(Location<World>) -> Boolean): Boolean {
        if (this.range > 0 && this.location.distanceSquared(this.origin) > this.rangeSquared) {
            return false
        }

        if (this.checkDiagonals && this.location.isNearDiagonalWall(this.direction)) {
            return false
        }

        val result: Boolean = this.block(this.location)
        if (!result) {
            return result
        }

        this.location = this.location.next()

        return true
    }

    fun Location<World>.next(): Location<World> =
        this.add(direction.mul(speedFactor))

    inline fun affectLocations(
        source: Player, affected: MutableCollection<Location<World>>,
        radius: Double, affect: (test: Location<World>) -> Boolean
    ) {
        for (test: Location<World> in this.location.getNearbyLocations(radius)) {
            if (test in affected) {
                continue
            }

            if (BuildProtectionService.getInstance().isProtected(source, test)) {
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

            if (PvpProtectionService.getInstance().isProtected(source, test)) {
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
 * @return True if any of the rays succeeded
 */
inline fun Iterable<FastRaycast>.advanceAll(block: FastRaycast.(location: Location<World>) -> Boolean): Boolean {
    var successful = false
    for (projectile: FastRaycast in this) {
        val result: Boolean = projectile.progress { location: Location<World> ->
            this.block(location)
        }

        if (result) {
            successful = true
        }
    }
    return successful
}