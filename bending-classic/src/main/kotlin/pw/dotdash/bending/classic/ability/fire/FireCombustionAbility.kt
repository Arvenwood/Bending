package pw.dotdash.bending.classic.ability.fire

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.explosion.Explosion
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.ORIGIN
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.ray.FastRaycast
import pw.dotdash.bending.api.util.eyeLocation
import pw.dotdash.bending.api.util.headDirection
import pw.dotdash.bending.api.util.isWater
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes

data class FireCombustionAbility(
    override val cooldownMilli: Long,
    val canBreakBlocks: Boolean,
    val damage: Double,
    val power: Float,
    val radius: Double,
    val range: Double,
    val speed: Double
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.FIRE_COMBUSTION) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        canBreakBlocks = node.getNode("canBreakBlocks").boolean,
        damage = node.getNode("damage").double,
        power = node.getNode("power").float,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    override fun prepare(cause: Cause, context: AbilityContext) {
        val player: Player = cause.first(Player::class.java).get()
        context[ORIGIN] = player.eyeLocation
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context.require(PLAYER)
        return !BuildProtectionService.getInstance().isProtected(player, context.require(ORIGIN))
    }

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)
        val origin: Location<World> = context.require(ORIGIN)
        val direction: Vector3d = player.headDirection.normalize()

        val raycast = FastRaycast(
            origin = origin,
            direction = direction,
            range = range,
            speed = speed,
            checkDiagonals = true
        )

        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            if (player.isRemoved) {
                return
            }

            raycast.progress { current: Location<World> ->
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
                    return
                }

                affectEntities(player, affectedEntities, radius) { test: Entity ->
                    if (test !is Living || test.uniqueId == player.uniqueId) {
                        false
                    } else {
                        createExplosion(forward, power, canBreakBlocks)
                        return
                    }
                }

                return@progress true
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