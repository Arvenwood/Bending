package arvenwood.bending.plugin.action

import arvenwood.bending.api.ability.AbilityResult
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.air.AirConstants
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.absoluteValue
import kotlin.random.Random

data class AirProjectile(
    val origin: Location<World>,
    val direction: Vector3d,
    val damage: Double,
    val pushFactorSelf: Double,
    val pushFactorOther: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val checkDiagonals: Boolean,
    val canExtinguishFlames: Boolean,
    val canCoolLava: Boolean
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

        if (this.location.blockType.isSolid() || this.location.blockType.isLiquid()) {
            // Stop if we've hit solid or liquid blocks.
            return ErrorWallReached
        }

        val next: Location<World> = this.location.add(this.direction.mul(this.speedFactor))

        block(next)

        // Move forward.
        this.location = next

        return Success
    }

    fun affectBlocks(source: Player, affected: MutableCollection<Location<World>>) {
        for (test: Location<World> in this.location.getNearbyLocations(this.radius)) {
            if (BuildProtectionService.get().isProtected(source, test)) {
                // Can't fight here!
                continue
            }

            if (test in affected) {
                // We've already opened this door, flicked this lever, etc.
                continue
            }

            if (this.canExtinguishFlames && test.blockType == BlockTypes.FIRE) {
                // Extinguish flames.
                test.blockType = BlockTypes.AIR
                test.extent.spawnParticles(AirConstants.EXTINGUISH_EFFECT, test.position)
                continue
            }

            if (this.canCoolLava && (this.location.blockType == BlockTypes.LAVA || this.location.blockType == BlockTypes.FLOWING_LAVA)) {
                when {
                    this.location.blockType == BlockTypes.FLOWING_LAVA -> this.location.blockType = BlockTypes.AIR
                    this.location.get(Keys.FLUID_LEVEL).get() == 0 -> this.location.blockType = BlockTypes.OBSIDIAN
                    else -> this.location.blockType = BlockTypes.COBBLESTONE
                }
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

    private fun doorSound(open: Boolean): SoundType =
        if (open) SoundTypes.BLOCK_WOODEN_DOOR_OPEN else SoundTypes.BLOCK_WOODEN_DOOR_CLOSE

    fun affectEntities(source: Player, affected: MutableCollection<Entity>, canPushSelf: Boolean) {
        for (entity: Entity in this.location.getNearbyEntities(this.radius)) {
            if (PvpProtectionService.get().isProtected(source, entity)) {
                // Can't fight here!
                continue
            }

            // Push the entity around.
            this.affectEntity(source, entity, affected, canPushSelf)
        }
    }

    private fun affectEntity(source: Player, target: Entity, affected: MutableCollection<Entity>, canPushSelf: Boolean) {
        val isSelf = target.uniqueId == source.uniqueId
        var knockback = this.pushFactorOther

        if (isSelf) {
            if (canPushSelf) {
                knockback = this.pushFactorSelf
            } else {
                // Ignore us.
                return
            }
        }

        val max: Double = this.speed / this.speedFactor

        var push = this.direction
        if (push.y.absoluteValue > max && !isSelf) {
            push = push.withY(if (push.y < 0) -max else max)
        }

        knockback *= (1 - target.position.distance(this.origin.position) / (2 * this.range))

        if (target.location.add(0.0, -0.5, 0.0).blockType.isSolid()) {
            knockback *= 0.85
        }

        push = push.normalize().mul(knockback)
        if (target.velocity.dot(push).absoluteValue > knockback && target.velocity.angle(push) > Math.PI / 3) {
            // Increase the velocity in their current direction.
            push = push.normalize().add(target.velocity).mul(knockback)
        }

        // Toss the target.
        target.velocity = push.min(4.0, 4.0, 4.0).max(-4.0, -4.0, -4.0)

        if (this.damage > 0 && target is Living && !isSelf && target !in affected) {
            // Hurt them.
            target.damage(this.damage, DamageSources.MAGIC)
            affected += target
        }

        if (target.get(Keys.FIRE_TICKS).orElse(0) > 0) {
            // Make the target stop, drop, and roll.
            target.offer(Keys.FIRE_TICKS, 0)
            target.world.spawnParticles(AirConstants.EXTINGUISH_EFFECT, target.position)
        }
    }

    fun visualize(effect: ParticleEffect, playSounds: Boolean) {
        this.location.spawnParticles(effect)

        if (playSounds) {
            this.location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, this.location.position, 0.5, 1.0)
        }
    }
}

inline fun Iterable<AirProjectile>.advanceAll(block: (projectile: AirProjectile, location: Location<World>) -> Unit): Boolean {
    var successful = false
    for (projectile: AirProjectile in this) {
        val result: AbilityResult = projectile.advance { location: Location<World> ->
            block(projectile, location)
        }

        if (result == Success) {
            successful = true
        }
    }
    return successful
}