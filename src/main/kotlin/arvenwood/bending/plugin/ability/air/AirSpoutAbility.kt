package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityResult.*
import arvenwood.bending.api.ability.StandardContext.player
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import com.flowpowered.math.vector.Vector3d
import kotlinx.coroutines.Job
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.asKotlinRandom

data class AirSpoutAbility(
    override val cooldown: Long,
    val duration: Long,
    val interval: Long,
    val height: Double
) : Ability<AirSpoutAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        duration = node.getNode("duration").long,
        interval = node.getNode("interval").long,
        height = node.getNode("height").double
    )

    override val type: AbilityType<AirSpoutAbility> = AbilityTypes.AIR_SPOUT

    private val particleEffect: ParticleEffect =
        ParticleEffect.builder()
            .type(ParticleTypes.CLOUD)
            .quantity(3)
            .offset(Vector3d(0.4, 0.4, 0.4))
            .build()

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context[player] ?: return ErrorNoTarget

        context[animationTime] = System.currentTimeMillis()
        context[angle] = 0

        val startTime: Long = System.currentTimeMillis()
        val defer: Job = BenderService.get()[player.uniqueId].deferExecution(this.type, LEFT_CLICK)
        abilityLoopUnsafe {
            if (player.isRemoved) {
                return Success
            }
            if (startTime + this.duration <= System.currentTimeMillis()) {
                return Success
            }
            if (defer.isCompleted) {
                return Success
            }

            val eyeLoc: Location<World> = player.eyeLocation
            if (eyeLoc.blockType.isWater() || eyeLoc.blockType.isSolid()) {
                return ErrorUnderWater
            }

            player.offer(Keys.FALL_DISTANCE, 0F)
            player.isSprinting = false

            if (Constants.RANDOM.nextInt(4) == 0) {
                // Play the sounds every now and then.
                player.world.playSound(SoundTypes.ENTITY_CREEPER_HURT, player.position, 0.5, 1.0)
            }

            val highest: Location<World> = player.location.asHighestLocation()
            val dy: Double = player.location.y - highest.y

            if (dy > this.height) {
                player.canFly = false
                player.isFlying = false
            } else {
                player.canFly = true
                player.isFlying = true
            }

            this.rotateAirColumn(context, player.location, highest)
        }
    }

    override fun cleanup(context: AbilityContext) {
        val player = context[player] ?: return
        player.canFly = false
        player.isFlying = false
    }

    private fun rotateAirColumn(context: AbilityContext, playerLocation: Location<World>, highest: Location<World>) {
        var animationTime: Long by context.by(animationTime)
        var angle: Int by context.by(angle)

        if (this.interval + animationTime <= System.currentTimeMillis()) {
            animationTime = System.currentTimeMillis()
            val location: Location<World> = Location(highest.extent, playerLocation.x, highest.y, playerLocation.z)

            var index = angle
            val dy = min(playerLocation.y - highest.y, this.height)
            angle = if (angle >= 8) 0 else angle + 1

            var i = 1
            while (i <= dy) {
                index = if (index >= 8) 0 else index + 1
                val effectLoc = location.add(0.0, i.toDouble(), 0.0)
                effectLoc.spawnParticles(this.particleEffect)
                i++
            }
        }
    }

    object animationTime : AbilityContext.Key<Long>("bending:animation_time", "Animation Time Context")

    object angle : AbilityContext.Key<Int>("bending:angle", "Angle Context")
}