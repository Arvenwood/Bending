package arvenwood.bending.plugin.projectile

import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.air.AirConstants
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.absoluteValue

object AirRaycast {

    fun extinguishFlames(test: Location<World>): Boolean {
        if (test.blockType != BlockTypes.FIRE) return false

        test.blockType = BlockTypes.AIR
        test.spawnParticles(AirConstants.EXTINGUISH_EFFECT)

        return true
    }

    fun coolLava(test: Location<World>): Boolean {
        if (test.blockType != BlockTypes.LAVA && test.blockType != BlockTypes.FLOWING_LAVA) return false

        when {
            test.blockType == BlockTypes.FLOWING_LAVA -> test.blockType = BlockTypes.AIR
            test.get(Keys.FLUID_LEVEL).get() == 0 -> test.blockType = BlockTypes.OBSIDIAN
            else -> test.blockType = BlockTypes.COBBLESTONE
        }

        return true
    }

    fun toggleDoor(test: Location<World>): Boolean {
        if (test.blockType !in AirConstants.DOORS) return false

        // Open/Close doors.
        val open: Boolean = test.get(Keys.OPEN).orElse(false)
        test.offer(Keys.OPEN, !open)
        test.extent.playSound(doorSound(!open), test.position, 0.5, 0.0)

        return true
    }

    private fun doorSound(open: Boolean): SoundType =
        if (open) SoundTypes.BLOCK_WOODEN_DOOR_OPEN else SoundTypes.BLOCK_WOODEN_DOOR_CLOSE

    fun toggleLever(test: Location<World>): Boolean {
        if (test.blockType != BlockTypes.LEVER) return false

        // Flip switches.
        test.offer(Keys.POWERED, test.get(Keys.POWERED).orElse(false))
        test.extent.playSound(SoundTypes.BLOCK_LEVER_CLICK, test.position, 0.5, 0.0)

        return true
    }

    fun Raycast.pushEntity(source: Player, target: Entity, canPushSelf: Boolean, pushFactorSelf: Double, pushFactorOther: Double): Boolean {
        val isSelf: Boolean = source.uniqueId == target.uniqueId
        var knockback: Double = pushFactorOther

        if (isSelf) {
            if (!canPushSelf) {
                // Ignore us.
                return false
            }

            knockback = pushFactorSelf
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

        push = push.normalize() * knockback
        if (target.velocity.dot(push).absoluteValue > knockback && target.velocity.angle(push) > Math.PI / 3) {
            // Increase the velocity in their current direction.
            push = push.normalize().add(target.velocity) * knockback
        }

        // Toss the target.
        target.velocity = push.min(4.0, 4.0, 4.0).max(-4.0, -4.0, -4.0)
        return true
    }
}