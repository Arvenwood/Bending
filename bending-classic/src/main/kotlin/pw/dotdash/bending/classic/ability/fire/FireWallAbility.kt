package pw.dotdash.bending.classic.ability.fire

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.math.max
import kotlin.random.Random

data class FireWallAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val displayInterval: Long,
    val damage: Double,
    val damageInterval: Long,
    val fireTicks: Int,
    val width: Int,
    val height: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.FIRE_WALL) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        displayInterval = node.getNode("displayInterval").long,
        damage = node.getNode("damage").double,
        damageInterval = node.getNode("damageInterval").long,
        fireTicks = node.getNode("fireTicks").int,
        width = node.getNode("width").int,
        height = node.getNode("height").int
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val damageRadiusSquared: Double = 1.5 * 1.5

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        val startTime: EpochTime = EpochTime.now()
        var displayTick: Long = 0
        var damageTick: Long = 0

        val origin: Location<World> = player.location.add(player.headDirection.mul(2.0))
        val locations: List<Location<World>> = calculateLocations(player, origin, player.headDirection)

        abilityLoopUnsafe {
            val curTime: EpochTime = EpochTime.now()

            if (startTime.elapsed(curTime) >= duration) {
                return
            }

            if (startTime.elapsed(curTime) >= displayTick * displayInterval) {
                displayTick++
                display(locations)
            }
            if (startTime.elapsed(curTime) >= damageTick * damageInterval) {
                damageTick++
                damage(player, origin, locations)
            }
        }
    }

    private fun calculateLocations(player: Player, origin: Location<World>, direction: Vector3d): List<Location<World>> {
        val result = ArrayList<Location<World>>()
        val horizontal: Vector3d = direction.getOrthogonal(0.0, 1.0).normalize()
        val vertical: Vector3d = direction.getOrthogonal(90.0, 1.0).normalize()

        for (i: Int in -this.width..this.width) {
            for (j: Int in -this.height..this.height) {
                val location: Location<World> = origin.add(vertical.mul(j.toDouble())).add(horizontal.mul(i.toDouble()))

                if (BuildProtectionService.getInstance().isProtected(player, location)) continue

                if (location !in result) {
                    result += location
                }
            }
        }

        return result
    }

    private fun display(locations: List<Location<World>>) {
        val flame: ParticleEffect = ParticleEffect.builder()
            .type(ParticleTypes.FLAME)
            .quantity(3)
            .offset(Vector3d(0.6, 0.6, 0.6))
            .build()
        val smoke: ParticleEffect = ParticleEffect.builder()
            .type(ParticleTypes.SMOKE)
            .quantity(2)
            .offset(Vector3d(0.6, 0.6, 0.6))
            .build()

        for (location: Location<World> in locations) {
            location.spawnParticles(flame)
            location.spawnParticles(smoke)

            if (Random.nextInt(7) == 0) {
                // Play fire bending sound, every now and then.
                location.extent.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, location.position, 0.5, 1.0)
            }
        }
    }

    private fun damage(source: Player, origin: Location<World>, locations: List<Location<World>>) {
        val radius: Int = max(this.width, this.height) + 1

        for (entity: Entity in origin.getNearbyEntities(radius.toDouble())) {
            if (entity is Player && PvpProtectionService.getInstance().isProtected(source, entity)) {
                // Can't fight here!
                continue
            }

            for (location: Location<World> in locations) {
                if (entity.location.distanceSquared(location) <= this.damageRadiusSquared) {
                    affect(entity)
                    break
                }
            }
        }
    }

    private fun affect(entity: Entity) {
        entity.velocity = Vector3d.ZERO
        if (entity is Living) {
            entity.damage(this.damage, DamageSources.MAGIC)
        }
        entity.offer(Keys.FIRE_TICKS, this.fireTicks * 20)
    }
}