package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.ProtectionService
import arvenwood.bending.api.util.*
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
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class FireWallAbility(
    override val cooldown: Long,
    val duration: Long,
    val displayInterval: Long,
    val damage: Double,
    val damageInterval: Long,
    val fireTicks: Int,
    val width: Int,
    val height: Int
) : Ability<FireWallAbility> {

    override val type: AbilityType<FireWallAbility> = FireWallAbility

    companion object : AbstractAbilityType<FireWallAbility>(
        element = Elements.Fire,
        executionTypes = enumSetOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:fire_wall",
        name = "FireWall"
    ) {
        override val default: Ability<FireWallAbility> = FireWallAbility(
            cooldown = 11000L,
            duration = 5000L,
            displayInterval = 250L,
            damage = 1.0,
            damageInterval = 500L,
            fireTicks = 0,
            width = 4,
            height = 4
        )

        override fun load(node: ConfigurationNode): FireWallAbility = FireWallAbility(
            cooldown = node.getNode("cooldown").long,
            duration = node.getNode("duration").long,
            displayInterval = node.getNode("displayInterval").long,
            damage = node.getNode("damage").double,
            damageInterval = node.getNode("damageInterval").long,
            fireTicks = node.getNode("fireTicks").int,
            width = node.getNode("width").int,
            height = node.getNode("height").int
        )
    }

    private val random: Random = java.util.Random().asKotlinRandom()

    override fun prepare(player: Player, context: AbilityContext) {
        context[origin] = player.location.add(player.headDirection.mul(2.0))
        context[direction] = player.headDirection
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player = context[player] ?: return ErrorNoTarget

        val startTime: Long = System.currentTimeMillis()
        var displayTick: Long = 0
        var damageTick: Long = 0

        val origin: Location<World> = context.require(origin)
        val locations: List<Location<World>> = calculateLocations(player, origin, context.require(direction))

        abilityLoopUnsafe {
            val curTime = System.currentTimeMillis()

            if (startTime + this.duration <= curTime) {
                return Success
            }

            if (startTime + displayTick * displayInterval <= curTime) {
                displayTick++
                display(locations)
            }
            if (startTime + damageTick * damageInterval <= curTime) {
                damageTick++
                damage(origin, locations)
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

                if (ProtectionService.get().isProtected(player, location)) continue

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

        for (location in locations) {
            location.spawnParticles(flame)
            location.spawnParticles(smoke)

            if (this.random.nextInt(7) == 0) {
                // Play fire bending sound, every now and then.
                location.extent.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, location.position, 0.5, 1.0)
            }
        }
    }

    private fun damage(origin: Location<World>, locations: List<Location<World>>) {
        val radius: Int = max(this.width, this.height) + 1
        for (entity: Entity in origin.getNearbyEntities(radius.toDouble())) {
            if (entity is Player && ProtectionService.get().isProtected(entity, entity.location)) {
                // Can't fight here!
                continue
            }

            for (location: Location<World> in locations) {
                if (entity.location.distanceSquared(location) <= 1.5 * 1.5) {
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