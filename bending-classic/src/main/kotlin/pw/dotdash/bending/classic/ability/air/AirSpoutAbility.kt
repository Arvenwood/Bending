package pw.dotdash.bending.classic.ability.air

import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.AbilityContext
import pw.dotdash.bending.api.ability.AbilityContextKeys.BENDER
import pw.dotdash.bending.api.ability.AbilityContextKeys.PLAYER
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.ability.CoroutineAbility
import pw.dotdash.bending.api.ability.CoroutineTask
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.api.util.*
import pw.dotdash.bending.classic.BendingClassic
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes
import java.util.concurrent.CompletableFuture
import kotlin.math.min
import kotlin.random.Random

data class AirSpoutAbility(
    override val cooldownMilli: Long,
    val duration: Long,
    val interval: Long,
    val height: Double
) : CoroutineAbility(cooldownMilli, ClassicAbilityTypes.AIR_SPOUT) {
    constructor(node: ConfigurationNode) : this(
        cooldownMilli = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        interval = node.getNode("interval").long,
        height = node.getNode("height").double
    )

    override val plugin: PluginContainer
        get() = BendingClassic.PLUGIN

    private val particleEffect: ParticleEffect = EffectService.getInstance().createParticle(Elements.AIR, 3, VectorUtil.VECTOR_0_4)

    override suspend fun CoroutineTask.activate(context: AbilityContext, executionType: AbilityExecutionType) {
        val player: Player = context.require(PLAYER)
        val bender: Bender = context.require(BENDER)

        var animationTime: EpochTime = EpochTime.now()

        val startTime: EpochTime = EpochTime.now()
        val defer: CompletableFuture<Void> = bender.waitForExecution(type, LEFT_CLICK)
        abilityLoopUnsafe {
            if (player.isRemoved) {
                defer.cancel(false)
                return
            }
            if (startTime.elapsedNow() >= duration) {
                defer.cancel(false)
                return
            }
            if (defer.isDone) {
                return
            }

            val eyeLocation: Location<World> = player.eyeLocation
            if (eyeLocation.blockType.isWater() || eyeLocation.blockType.isSolid()) {
                defer.cancel(false)
                return
            }

            player.fallDistance = 0F
            player.isSprinting = false

            if (Random.nextInt(4) == 0) {
                // Play the sounds every now and then.
                player.world.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.position, 0.5, 1.0)
            }

            val highest: Location<World> = player.location.asHighestLocation()
            val dy: Double = player.location.y - highest.y

            if (dy > height) {
                player.canFly = false
                player.isFlying = false
            } else {
                player.canFly = true
                player.isFlying = true
            }

            if (animationTime.elapsedNow() >= interval) {
                animationTime = EpochTime.now()

                val location: Location<World> = Location(highest.extent, player.location.x, highest.y, player.location.z)
                val dyParticle: Double = min(player.location.y - highest.y, height)

                var i = 1
                while (i <= dyParticle) {
                    location.add(0.0, i.toDouble(), 0.0).spawnParticles(particleEffect)
                    i++
                }
            }
        }
    }

    override fun cleanup(context: AbilityContext) {
        val player: Player = context.require(PLAYER)
        player.canFly = false
        player.isFlying = false
    }
}