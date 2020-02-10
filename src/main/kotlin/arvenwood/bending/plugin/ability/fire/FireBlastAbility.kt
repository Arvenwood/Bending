package arvenwood.bending.plugin.ability.fire

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.ability.StandardContext.origin
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.raycast.Raycast
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
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
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

    override val type: AbilityType<FireBlastAbility> = AbilityTypes.FIRE_BLAST

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

    override fun prepare(player: Player, context: AbilityContext) {
        context[origin] = player.eyeLocation
        context[direction] = player.headDirection.normalize()
    }

    override fun validate(context: AbilityContext): Boolean {
        val player: Player = context.player
        val origin: Location<World> = context.require(origin)
        return !BuildProtectionService.get().isProtected(player, origin)
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
        abilityLoop {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return ErrorDied
            }

            val result: AbilityResult = raycast.advance { current: Location<World> ->
                if (BuildProtectionService.get().isProtected(player, current)) return@advance ErrorProtected

                affectEntities(player, affectedEntities, radius) { test: Entity ->
                    affect(player, test)
                }

                if (showParticles) {
                    playParticles(particleFlame)
                    playParticles(particleSmoke)
                }

                if (Constants.RANDOM.nextInt(4) == 0) {
                    playSounds(SoundTypes.BLOCK_FIRE_AMBIENT, 0.5, 1.0)
                }

                if (affectedEntities.size > 0) return Success
                if (current.blockType.isSolid() || current.blockType.isLiquid()) return Success

                return@advance Success
            }

            if (result != Success) {
                return result
            }
        }

        return Success
    }

    private fun Raycast.affect(source: Player, target: Entity): Boolean {
        if (target.uniqueId == source.uniqueId) return false
        if (PvpProtectionService.get().isProtected(source, target)) return false

        target.velocity = direction.mul(knockback)

        if (target is Living) {
            target.offer(Keys.FIRE_TICKS, fireTicks * 20)
            target.damage(damage, DamageSources.FIRE_TICK)
            return true
        }

        return false
    }
}