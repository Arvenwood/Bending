package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.SNEAK
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.*
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.ray.AirRaycast
import pw.dotdash.bending.api.ray.FastRaycast
import pw.dotdash.bending.api.ray.pushEntity
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

data class AirBlastAbility(
    override val cooldownMilli: Long,
    val damage: Double,
    val knockbackSelf: Double,
    val knockbackOther: Double,
    val radius: Double,
    val range: Double,
    val selectRange: Double,
    val speed: Double,
    val canFlickLevers: Boolean,
    val canOpenDoors: Boolean,
    val canPressButtons: Boolean,
    val canCoolLava: Boolean,
    val numParticles: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_BLAST) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        damage = node.getNode("damage").double,
        knockbackSelf = node.getNode("knockback").double,
        knockbackOther = node.getNode("knockbackOther").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        selectRange = node.getNode("selectRange").double,
        speed = node.getNode("speed").double,
        canFlickLevers = node.getNode("canFlickLevers").boolean,
        canOpenDoors = node.getNode("canOpenDoors").boolean,
        canPressButtons = node.getNode("canPressButtons").boolean,
        canCoolLava = node.getNode("canCoolLava").boolean,
        numParticles = node.getNode("numParticles").int
    )

    private val particleEffect: ParticleEffect =
        EffectService.getInstance().createParticle(Elements.AIR, this.numParticles, VectorUtil.VECTOR_0_275)

    override fun prepare(cause: Cause, context: AbilityContext) {
        val player: Player = cause.first(Player::class.java).get()

        context[ORIGIN] = player.eyeLocation
    }

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)

        when (executionType) {
            LEFT_CLICK -> this.runLeftClickMode(context, player, false)
            SNEAK -> this.runSneakMode(context, player)
        }
    }

    private suspend fun CoroutineTask.runSneakMode(context: AbilityContext, player: Player) {
        val origin: Location<World> = player.getTargetLocation(selectRange)

        context[ORIGIN] = origin

        val bender: Bender = context.require(BENDER)
        val defer: CompletableFuture<Void> = bender.waitForExecution(type, LEFT_CLICK)
        abilityLoop {
            if (player.isRemoved) {
                defer.cancel(false)
                return
            }
            if (origin.distanceSquared(player.eyeLocation) > selectRange * selectRange) {
                defer.cancel(false)
                return
            }

            origin.spawnParticles(EffectService.getInstance().createRandomParticle(Elements.AIR, 4))

            if (defer.isDone) {
                if (defer.isCancelled) {
                    return
                }

                this.runLeftClickMode(context, player, true)
                return
            }
        }

        return
    }

    private suspend fun CoroutineTask.runLeftClickMode(context: AbilityContext, player: Player, canPushSelf: Boolean) {
        if (player.eyeLocation.blockType.isLiquid()) return

        val affectedLocations = HashSet<Location<World>>()
        val affectedEntities = HashSet<Entity>()
        val origin: Location<World> = context.require(ORIGIN)
        val direction: Vector3d = player.headDirection.normalize()

        val raycast = FastRaycast(
            origin = origin,
            direction = direction,
            range = range,
            speed = speed,
            checkDiagonals = true
        )

        abilityLoopUnsafe {
            if (player.isRemoved) {
                // Stop if this Player object is stale.
                return
            }

            val result: Boolean = raycast.progress { current: Location<World> ->
                if (BuildProtectionService.getInstance().isProtected(player, current)) {
                    return@progress false
                }

                affectLocations(player, affectedLocations, radius) { test: Location<World> ->
                    AirRaycast.extinguishFlames(test)
                            || (canCoolLava && AirRaycast.coolLava(test))
                            || (canOpenDoors && AirRaycast.toggleDoor(test))
                            || (canFlickLevers && AirRaycast.toggleLever(test))
                }
                affectEntities(player, affectedEntities, radius) { test: Entity ->
                    this.pushEntity(player, test, canPushSelf, knockbackSelf, knockbackOther)

                    damageEntity(test, damage)
                }
                playParticles(particleEffect)

                if (Random.nextInt(4) == 0) {
                    playSounds(SoundTypes.ENTITY_CREEPER_HURT, 0.5, 1.0)
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
}