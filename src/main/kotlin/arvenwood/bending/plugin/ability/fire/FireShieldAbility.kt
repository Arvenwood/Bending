package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.util.getNearbyEntities
import arvenwood.bending.api.util.getNearbyLocations
import arvenwood.bending.api.util.spawnParticles
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.projectile.Projectile
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin

data class FireShieldAbility(
    override val cooldown: Long,
    val duration: Long,
    val radius: Double,
    val fireTicks: Int
) : Ability<FireShieldAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        radius = node.getNode("radius").double,
        fireTicks = node.getNode("fireTicks").int
    )

    override val type: AbilityType<FireShieldAbility> = AbilityTypes.FIRE_SHIELD

    private val particleFlame: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(1)
        .offset(Vector3d(0.1, 0.1, 0.1))
        .build()

    private val particleSmoke: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(1)
        .offset(Vector3d.ZERO)
        .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(player)

        var increment = 20
        val startTime: Long = System.currentTimeMillis()
        abilityLoopUnsafe {
            if (!player.getOrElse(Keys.IS_SNEAKING, false)) {
                return Success
            }
            if (this.duration > 0 && startTime + this.duration <= System.currentTimeMillis()) {
                return Success
            }

            for (theta: Int in 0 until 180 step increment) {
                for (phi: Int in 0 until 360 step increment) {
                    val rTheta: Double = Math.toRadians(theta.toDouble())
                    val rPhi: Double = Math.toRadians(phi.toDouble())

                    val displayLoc: Location<World> = player.location.add(
                        this.radius / 1.5 * cos(rPhi) * sin(rTheta),
                        this.radius / 1.5 * cos(rTheta),
                        this.radius / 1.5 * sin(rPhi) * sin(rTheta)
                    )

                    if (Constants.RANDOM.nextInt(6) == 0) {
                        displayLoc.spawnParticles(this.particleFlame)
                    }
                    if (Constants.RANDOM.nextInt(4) == 0) {
                        displayLoc.spawnParticles(this.particleSmoke)
                    }
                    if (Constants.RANDOM.nextInt(7) == 0) {
                        // Play fire bending sound, every now and then.
                        player.world.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, player.position, 0.5, 1.0)
                    }
                }
            }

            increment += 20
            if (increment >= 70) {
                increment = 20
            }

            for (test: Location<World> in player.location.getNearbyLocations(this.radius)) {
                if (test.blockType == BlockTypes.FIRE) {
                    test.blockType = BlockTypes.AIR
                    test.extent.playSound(SoundTypes.BLOCK_FIRE_EXTINGUISH, test.position, 0.5, 1.0)
                }
            }

            for (entity: Entity in player.location.getNearbyEntities(this.radius)) {
                if (PvpProtectionService.get().isProtected(player, entity)) {
                    continue
                } else if (entity is Living) {
                    if (player.uniqueId == entity.uniqueId) continue

                    entity.offer(Keys.FIRE_TICKS, this.fireTicks * 20)
                } else if (entity is Projectile) {
                    entity.remove()
                }
            }
        }
    }
}