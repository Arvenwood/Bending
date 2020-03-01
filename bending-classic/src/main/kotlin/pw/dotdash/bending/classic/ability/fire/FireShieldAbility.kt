package pw.dotdash.bending.classic.ability.fire

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
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.util.EpochTime
import pw.dotdash.bending.api.util.getNearbyEntities
import pw.dotdash.bending.api.util.getNearbyLocations
import pw.dotdash.bending.api.util.spawnParticles
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FireShieldAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val radius: Double,
    val fireTicks: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.FIRE_SHIELD) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        radius = node.getNode("radius").double,
        fireTicks = node.getNode("fireTicks").int
    )

    private val particleFlame: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(3)
        .offset(Vector3d(0.1, 0.1, 0.1))
        .build()

    private val particleSmoke: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(3)
        .offset(Vector3d.ZERO)
        .build()

    private val offsets: Array<List<Vector3d>> = this.calculateOffsets()

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        var index = 0
        val startTime: EpochTime = EpochTime.now()
        abilityLoopUnsafe {
            if (!player.getOrElse(Keys.IS_SNEAKING, false)) {
                return
            }
            if (duration > 0 && startTime.elapsedNow() >= duration) {
                return
            }

            for (offset: Vector3d in offsets[index]) {
                val displayLocation: Location<World> = player.location.add(offset)

                if (Random.nextInt(6) == 0) {
                    displayLocation.spawnParticles(particleFlame)
                }
                if (Random.nextInt(4) == 0) {
                    displayLocation.spawnParticles(particleSmoke)
                }
                if (Random.nextInt(7) == 0) {
                    // Play fire bending sound, every now and then.
                    player.world.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, player.position, 0.5, 1.0)
                }
            }

            index++
            if (index == 3) {
                index = 0
            }

            for (test: Location<World> in player.location.getNearbyLocations(radius)) {
                if (test.blockType == BlockTypes.FIRE) {
                    test.blockType = BlockTypes.AIR
                    test.extent.playSound(SoundTypes.BLOCK_FIRE_EXTINGUISH, test.position, 0.5, 1.0)
                }
            }

            for (entity: Entity in player.location.getNearbyEntities(radius)) {
                if (PvpProtectionService.getInstance().isProtected(player, entity)) {
                    continue
                } else if (entity is Living) {
                    if (player.uniqueId == entity.uniqueId) continue

                    entity.offer(Keys.FIRE_TICKS, fireTicks * 20)
                } else if (entity is Projectile) {
                    entity.remove()
                }
            }
        }
    }

    private fun calculateOffsets(): Array<List<Vector3d>> =
        Array(3) { index: Int ->
            val offsets = ArrayList<Vector3d>()
            val increment: Int = index * 20 + 20

            for (theta: Int in 0 until 180 step increment) {
                val thetaRad: Double = Math.toRadians(theta.toDouble())
                val sinTheta: Double = sin(thetaRad)
                val cosTheta: Double = cos(thetaRad)

                for (phi: Int in 0 until 360 step increment) {
                    val phiRad: Double = Math.toRadians(phi.toDouble())

                    offsets += Vector3d(
                        this.radius / 1.5 * cos(phiRad) * sinTheta,
                        this.radius / 1.5 * cosTheta,
                        this.radius / 1.5 * sin(phiRad) * sinTheta
                    )
                }
            }

            offsets
        }
}