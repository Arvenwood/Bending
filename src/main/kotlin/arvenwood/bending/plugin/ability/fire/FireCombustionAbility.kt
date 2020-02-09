package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorNoTarget
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.AbilityTypes
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.explosion.Explosion

data class FireCombustionAbility(
    override val cooldown: Long,
    val canBreakBlocks: Boolean,
    val damage: Double,
    val power: Float,
    val radius: Double,
    val range: Double,
    val speed: Double
) : Ability<FireCombustionAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        canBreakBlocks = node.getNode("canBreakBlocks").boolean,
        damage = node.getNode("damage").double,
        power = node.getNode("power").float,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double
    )

    override val type: AbilityType<FireCombustionAbility> = AbilityTypes.FIRE_COMBUSTION

    private val speedFactor: Double = this.speed * (50 / 1000.0)
    private val rangeSquared: Double = this.range * this.range

    override fun prepare(player: Player, context: AbilityContext) {
        context[StandardContext.direction] = player.headDirection.normalize()
        context[StandardContext.origin] = player.eyeLocation
        context[StandardContext.currentLocation] = player.eyeLocation
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context.require(StandardContext.player)
        return !BuildProtectionService.get().isProtected(player, player.location)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player = context[StandardContext.player] ?: return ErrorNoTarget

        var location: Location<World> by context.by(StandardContext.currentLocation)
        val direction: Vector3d = context.require(StandardContext.direction)
        val origin: Location<World> = context.require(StandardContext.origin)

        abilityLoop {
            if (location.distanceSquared(origin) > this.rangeSquared) {
                return Success
            }

            if (location.blockType != BlockTypes.AIR && !location.blockType.isWater()) {
                createExplosion(location, this.power, this.canBreakBlocks)
                return Success
            }

            for (entity: Entity in location.getNearbyEntities(this.radius)) {
                if (entity !is Living) continue
                if (entity.uniqueId == player.uniqueId) continue

                createExplosion(location, this.power, this.canBreakBlocks)
                return Success
            }

            location = advanceLocation(location, direction)
        }

        return Success
    }

    /**
     * Calculate the next location to blast.
     *
     * @return The next location, or null if a wall is hit.
     */
    private fun advanceLocation(location: Location<World>, direction: Vector3d): Location<World> {
        val fireworks: ParticleEffect = ParticleEffect.builder()
            .type(ParticleTypes.FIREWORKS_SPARK)
            .quantity(5)
            .offset(Vector3d(Math.random() / 2, Math.random() / 2, Math.random() / 2))
            .build()
        val flame: ParticleEffect = ParticleEffect.builder()
            .type(ParticleTypes.FLAME)
            .quantity(2)
            .offset(Vector3d(Math.random() / 2, Math.random() / 2, Math.random() / 2))
            .build()
        location.spawnParticles(fireworks)
        location.spawnParticles(flame)
        location.extent.playSound(SoundTypes.ENTITY_FIREWORK_BLAST, location.position, 0.5, 1.0)

        return location.add(direction.mul(this.speedFactor))
    }

    /**
     * Creates an explosion at the target location.
     */
    private fun createExplosion(location: Location<World>, power: Float, canBreakBlocks: Boolean) {
        val explosion = Explosion.builder()
            .location(location)
            .radius(power)
            .shouldDamageEntities(true)
            .shouldPlaySmoke(true)
            .canCauseFire(true)
            .shouldBreakBlocks(canBreakBlocks)
            .build()

        location.extent.triggerExplosion(explosion)
    }
}