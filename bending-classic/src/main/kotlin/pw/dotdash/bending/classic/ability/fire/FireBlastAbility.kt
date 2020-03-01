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
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.ORIGIN
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.ray.FastRaycast
import pw.dotdash.bending.api.util.eyeLocation
import pw.dotdash.bending.api.util.headDirection
import pw.dotdash.bending.api.util.isLiquid
import pw.dotdash.bending.api.util.isSolid
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import kotlin.random.Random

data class FireBlastAbility(
    override val cooldownMilli: Long,
    val damage: Double,
    val fireTicks: Int,
    val knockback: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val showParticles: Boolean,
    val flameRadius: Double,
    val smokeRadius: Double
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.FIRE_BLAST) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        damage = node.getNode("damage").double,
        fireTicks = node.getNode("fireTicks").int,
        knockback = node.getNode("knockback").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        showParticles = node.getNode("showParticles").boolean,
        flameRadius = node.getNode("flameRadius").double,
        smokeRadius = node.getNode("smokeRadius").double
    )

    private val particleFlame: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.FLAME)
        .quantity(6)
        .offset(Vector3d(this.flameRadius, this.flameRadius, this.flameRadius))
        .build()

    private val particleSmoke: ParticleEffect = ParticleEffect.builder()
        .type(ParticleTypes.SMOKE)
        .quantity(3)
        .offset(Vector3d(this.smokeRadius, this.smokeRadius, this.smokeRadius))
        .build()

    override fun prepare(cause: Cause, context: AbilityContext) {
        val player: Player = cause.first(Player::class.java).get()
        context[ORIGIN] = player.eyeLocation
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context.require(PLAYER)
        val origin: Location<World> = context.require(ORIGIN)
        return !BuildProtectionService.getInstance().isProtected(player, origin)
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
        abilityLoop {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return
            }

            val result: Boolean = raycast.progress { current: Location<World> ->
                if (BuildProtectionService.getInstance().isProtected(player, current)) {
                    return@progress false
                }

                affectEntities(player, affectedEntities, radius) { test: Entity ->
                    affect(player, test)
                }

                if (showParticles) {
                    playParticles(particleFlame)
                    playParticles(particleSmoke)
                }

                if (Random.nextInt(4) == 0) {
                    playSounds(SoundTypes.BLOCK_FIRE_AMBIENT, 0.5, 1.0)
                }

                if (affectedEntities.size > 0) {
                    return
                }
                if (current.blockType.isSolid() || current.blockType.isLiquid()) {
                    return
                }

                return@progress true
            }

            if (!result) {
                return
            }
        }
    }

    private fun FastRaycast.affect(source: Player, target: Entity): Boolean {
        if (target.uniqueId == source.uniqueId) {
            return false
        }
        if (PvpProtectionService.getInstance().isProtected(source, target)) {
            return false
        }

        target.velocity = direction.mul(knockback)

        if (target is Living) {
            target.offer(Keys.FIRE_TICKS, fireTicks * 20)
            target.damage(damage, DamageSources.FIRE_TICK)
            return true
        }

        return false
    }
}