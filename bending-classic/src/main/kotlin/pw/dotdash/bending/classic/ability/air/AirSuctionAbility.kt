package pw.dotdash.bending.classic.ability.air

import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.*
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.SNEAK
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

data class AirSuctionAbility(
    override val cooldownMilli: Long,
    val damage: Double,
    val radius: Double,
    val range: Double,
    val selectRange: Double,
    val speed: Double,
    val pushFactorSelf: Double,
    val pushFactorOther: Double,
    val canCoolLava: Boolean,
    val canFlickLevers: Boolean,
    val canOpenDoors: Boolean,
    val numParticles: Int
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_SUCTION) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        damage = node.getNode("damage").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        selectRange = node.getNode("selectRange").double,
        speed = node.getNode("speed").double,
        pushFactorSelf = node.getNode("pushFactorSelf").double,
        pushFactorOther = node.getNode("pushFactorOther").double,
        canCoolLava = node.getNode("canCoolLava").boolean,
        canFlickLevers = node.getNode("canFlickLevers").boolean,
        canOpenDoors = node.getNode("canOpenDoors").boolean,
        numParticles = node.getNode("numParticles").int
    )

    private val particleEffect: ParticleEffect =
        EffectService.getInstance().createParticle(Elements.AIR, this.numParticles, VectorUtil.VECTOR_0_275)

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

                context[DIRECTION] = player.headDirection.normalize()

                this.runLeftClickMode(context, player, true)
                return
            }
        }
    }

    private suspend fun CoroutineTask.runLeftClickMode(context: AbilityContext, player: Player, canPushSelf: Boolean) {
        if (player.eyeLocation.blockType.isLiquid()) {
            return
        }

        val affectedLocations: MutableCollection<Location<World>> = context.require(AFFECTED_LOCATIONS)
        val affectedEntities: MutableCollection<Entity> = context.require(AFFECTED_ENTITIES)
        val origin: Location<World> = context.require(ORIGIN)
        val direction: Vector3d = context.require(DIRECTION)

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

            val succeeded: Boolean = raycast.progress { current: Location<World> ->
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
                    pushEntity(player, test, canPushSelf, pushFactorSelf, pushFactorOther)

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

            if (!succeeded) {
                return
            }
        }
    }
}