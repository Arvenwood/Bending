package arvenwood.bending.plugin.action

import arvenwood.bending.api.ability.AbilityContext
import arvenwood.bending.api.ability.AbilityResult
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext
import arvenwood.bending.api.ability.require
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.air.AirBlastAbility
import arvenwood.bending.plugin.ability.air.AirConstants
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.absoluteValue

data class ParticleProjectile(
    val origin: Location<World>,
    val direction: Vector3d,
    val speed: Double,
    val range: Double,
    val checkDiagonals: Boolean
) {
    init {
        require(this.direction != Vector3d.ZERO) { "direction must be a non-zero vector" }
        require(this.speed > 0) { "speed must be a positive number" }
    }

    val speedFactor: Double = this.speed * (50 / 1000.0)
    val rangeSquared: Double = this.range * this.range

    var location: Location<World> = this.origin

    inline fun advance(block: (Location<World>) -> Unit): AbilityResult {
        if (this.range > 0 && this.location.distanceSquared(this.origin) > this.rangeSquared) {
            // Range limit reached!
            return ErrorOutOfRange
        }

        if (this.checkDiagonals && this.location.isNearDiagonalWall(this.direction)) {
            // Stop if we've hit a diagonal wall.
            return ErrorWallReached
        }

        block(this.location)

        // Move forward.
        this.location = this.location.add(this.direction.mul(this.speedFactor))

        return Success
    }

    companion object {
        @JvmStatic
        private fun doorSound(open: Boolean): SoundType =
            if (open) SoundTypes.BLOCK_WOODEN_DOOR_OPEN else SoundTypes.BLOCK_WOODEN_DOOR_CLOSE

        @JvmStatic
        fun affectBlocks(origin: Location<World>, radius: Double, player: Player, affected: MutableCollection<Location<World>>) {

            for (test: Location<World> in origin.getNearbyLocations(radius)) {
                if (BuildProtectionService.get().isProtected(player, test)) {
                    // Can't fight here!
                    continue
                }

                if (test.blockType == BlockTypes.FIRE) {
                    // Extinguish flames.
                    test.blockType = BlockTypes.AIR
                    test.extent.spawnParticles(AirBlastAbility.EXTINGUISH_EFFECT, test.position)
                    continue
                }

                if (test in affected) {
                    // We've already opened this door, flicked this lever, etc.
                    continue
                }

                if (test.blockType in AirConstants.DOORS) {
                    // Open/Close doors.
                    val open: Boolean = test.get(Keys.OPEN).orElse(false)
                    test.offer(Keys.OPEN, !open)
                    test.extent.playSound(doorSound(!open), test.position, 0.5, 0.0)
                    affected += test
                } else if (test.blockType == BlockTypes.LEVER) {
                    // Flip switches.
                    test.offer(Keys.POWERED, test.get(Keys.POWERED).orElse(false))
                    test.extent.playSound(SoundTypes.BLOCK_LEVER_CLICK, test.position, 0.5, 0.0)
                    affected += test
                }
            }
        }

        @JvmStatic
        fun affectEntities(
            location: Location<World>, player: Player, origin: Location<World>, direction: Vector3d,
            affected: MutableCollection<Entity>, fromAlternate: Boolean,
            radius: Double, pushFactor: Double, pushFactorOther: Double, speed: Double, speedFactor: Double, range: Double, damage: Double
        ) {
            for (entity: Entity in location.getNearbyEntities(radius)) {
                if (PvpProtectionService.get().isProtected(player, entity)) {
                    // Can't fight here!
                    continue
                }

                // Push the entity around.
                affectEntity(
                    player, origin, direction,
                    affected, entity, fromAlternate,
                    pushFactor, pushFactorOther, speed, speedFactor, range, damage
                )
            }
        }

        @JvmStatic
        private fun affectEntity(
            player: Player, origin: Location<World>, direction: Vector3d,
            affected: MutableCollection<Entity>, entity: Entity, fromAlternate: Boolean,
            pushFactor: Double, pushFactorOther: Double, speed: Double, speedFactor: Double, range: Double, damage: Double
        ) {
            val isSelf = entity.uniqueId == player.uniqueId
            var knockback = pushFactorOther

            if (isSelf) {
                if (fromAlternate) {
                    knockback = pushFactor
                } else {
                    // Ignore us.
                    return
                }
            }

            val max: Double = speed / speedFactor

            var push = direction
            if (push.y.absoluteValue > max && !isSelf) {
                push = push.withY(if (push.y < 0) -max else max)
            }

            knockback *= (1 - entity.position.distance(origin.position) / (2 * range))

            if (entity.location.add(0.0, -0.5, 0.0).blockType.isSolid()) {
                knockback *= 0.85
            }

            push = push.normalize().mul(knockback)
            if (entity.velocity.dot(push).absoluteValue > knockback && entity.velocity.angle(push) > Math.PI / 3) {
                // Increase the velocity in their current direction.
                push = push.normalize().add(entity.velocity).mul(knockback)
            }

            // Toss the entity.
            entity.velocity = push.min(4.0, 4.0, 4.0).max(-4.0, -4.0, -4.0)

            if (damage > 0 && entity is Living && entity.uniqueId != player.uniqueId && entity !in affected) {
                // Hurt them.
                entity.damage(damage, DamageSources.MAGIC)
                affected.add(entity)
            }

            if (entity.get(Keys.FIRE_TICKS).orElse(0) > 0) {
                // Make the entity stop, drop, and roll.
                entity.offer(Keys.FIRE_TICKS, 0)
                entity.world.spawnParticles(AirBlastAbility.EXTINGUISH_EFFECT, entity.position)
            }
        }
    }
}

inline fun Iterable<ParticleProjectile>.advanceAll(block: (Location<World>) -> Unit): Boolean {
    var successful = false
    for (projectile in this) {
        if (projectile.advance(block) == Success) {
            successful = true
        }
    }
    return successful
}