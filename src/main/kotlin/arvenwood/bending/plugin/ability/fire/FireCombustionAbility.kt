package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.raycast.Raycast
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

    override fun prepare(player: Player, context: AbilityContext) {
        context[direction] = player.headDirection.normalize()
        context[origin] = player.eyeLocation
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context.player
        return !BuildProtectionService.get().isProtected(player, player.location)
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.player

        val origin: Location<World> = context.require(origin)
        val direction: Vector3d = context.require(direction)

        val raycast = Raycast(
            origin = origin,
            direction = direction,
            range = this.range,
            speed = this.speed,
            checkDiagonals = true
        )

        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            if (player.isRemoved) return ErrorDied

            raycast.advance { current: Location<World> ->
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

                playParticles(fireworks)
                playParticles(flame)
                playSounds(SoundTypes.ENTITY_FIREWORK_BLAST, 0.5, 1.0)

                val forward: Location<World> = current.next()

                if (forward.blockType != BlockTypes.AIR && !forward.blockType.isWater()) {
                    createExplosion(forward, power, canBreakBlocks)
                    return Success
                }

                affectEntities(player, affectedEntities, radius) { test: Entity ->
                    if (test !is Living || test.uniqueId == player.uniqueId) {
                        false
                    } else {
                        createExplosion(forward, power, canBreakBlocks)
                        return Success
                    }
                }

                return@advance Success
            }
        }
    }

    /**
     * Creates an explosion at the target location.
     */
    private fun createExplosion(location: Location<World>, power: Float, canBreakBlocks: Boolean) {
        val explosion: Explosion = Explosion.builder()
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