package arvenwood.bending.api.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.ability.StandardContext.currentLocation
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
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class FireBlastAbility(
    override val cooldown: Long,
    val damage: Double,
    val fireTicks: Int,
    val knockback: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val showParticles: Boolean,
    val flameRadius: Double,
    val smokeRadius: Double
) : Ability<FireBlastAbility> {

    override val type: AbilityType<FireBlastAbility> = FireBlastAbility

    companion object : AbstractAbilityType<FireBlastAbility>(
        element = Elements.Fire,
        executionTypes = setOf(AbilityExecutionType.LEFT_CLICK),
        id = "bending:fire_blast",
        name = "FireBlast"
    ) {
        override val default: Ability<FireBlastAbility> = FireBlastAbility(
            cooldown = 1500L,
            damage = 3.0,
            fireTicks = 0,
            knockback = 0.3,
            radius = 1.5,
            range = 20.0,
            speed = 20.0,
            showParticles = true,
            flameRadius = 0.275,
            smokeRadius = 0.3
        )

        override fun load(node: ConfigurationNode): FireBlastAbility {
            TODO()
        }

        private const val MAX_TICKS: Int = 10000
    }

    private val speedFactor: Double = this.speed * (50 / 1000.0)
    private val rangeSquared: Double = this.range * this.range

    private val random: Random = java.util.Random().asKotlinRandom()

    private val particleFlame: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(6)
        .offset(Vector3d(this.flameRadius, this.flameRadius, this.flameRadius))
        .build()

    private val particleSmoke: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(3)
        .offset(Vector3d(this.flameRadius, this.flameRadius, this.flameRadius))
        .build()

    override fun prepare(player: Player, context: AbilityContext) {
        context[origin] = player.eyeLocation
        context[direction] = player.headDirection.normalize()
        context[currentLocation] = player.eyeLocation
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[player] ?: return ErrorNoTarget

        var location: Location<World> by context.by(currentLocation)
        val origin: Location<World> = context.require(origin)
        val direction: Vector3d = context.require(direction)

        abilityLoop(MAX_TICKS) {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return AbilityResult.ErrorDied
            }

            if (location.distanceSquared(origin) > this.rangeSquared) return Success
            if (location.blockType.isSolid() || location.blockType.isLiquid()) return Success

            val closest = location.getClosestEntity(radius)
            if (closest != null && affect(context, closest)) return Success

            // Move to the next position.
            location = advanceLocation(location, direction) ?: return Success
        }

        return Success
    }

    private fun affect(context: AbilityContext, entity: Entity): Boolean {
        val player: Player = context.require(player)

        if (entity.uniqueId == player.uniqueId) return false
        if (ProtectionService.get().isProtected(player, entity.location)) return false

        val direction: Vector3d = context.require(direction)

        entity.velocity = direction.mul(this.knockback)

        if (entity is Living) {
            entity.offer(Keys.FIRE_TICKS, this.fireTicks * 20)
            entity.damage(this.damage, DamageSources.MAGIC)
            return true
        }

        return false
    }

    private fun advanceLocation(location: Location<World>, direction: Vector3d): Location<World>? {
        if (this.showParticles) {
            // Show the particles.
            location.spawnParticles(particleFlame)
            location.spawnParticles(particleSmoke)
        }
        if (this.random.nextInt(4) == 0) {
            // Play fire bending sound, every now and then.
            location.extent.playSound(SoundTypes.BLOCK_FIRE_AMBIENT, location.position, 0.5, 1.0)
        }
        if (location.isNearDiagonalWall(direction)) {
            // Stop if we've hit a diagonal wall.
            return null
        }

        // Move forward.
        return location.add(direction.mul(this.speedFactor))
    }
}