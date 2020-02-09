package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.*
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.projectile.AirRaycast
import arvenwood.bending.plugin.projectile.Raycast
import arvenwood.bending.plugin.projectile.advanceAll
import arvenwood.bending.plugin.util.forInclusive
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin

data class AirSwipeAbility(
    override val cooldown: Long,
    val chargeTime: Long,
    val maxChargeFactor: Double,
    val arcDegrees: Double,
    val arcIncrementDegrees: Double,
    val damage: Double,
    val pushFactor: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val numParticles: Int
) : Ability<AirSwipeAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        chargeTime = node.getNode("chargeTime").long,
        maxChargeFactor = node.getNode("maxChargeFactor").double,
        arcDegrees = node.getNode("arcDegrees").double,
        arcIncrementDegrees = node.getNode("arcIncrementDegrees").double,
        damage = node.getNode("damage").double,
        pushFactor = node.getNode("pushFactor").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        numParticles = node.getNode("numParticles").int
    )

    override val type: AbilityType<AirSwipeAbility> = AbilityTypes.AIR_SWIPE

    private val arcRadians: Double = Math.toRadians(this.arcDegrees)
    private val arcDegreesRadians: Double = Math.toRadians(this.arcIncrementDegrees)

    private val particleEffect: ParticleEffect = EffectService.get().createParticle(Elements.AIR, this.numParticles, AirConstants.VECTOR_0_2)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        when (executionType) {
            LEFT_CLICK -> {
                return this.swipe(player, player.eyeLocation)
            }
            else -> {
                var charged = false
                val startTime: Long = System.currentTimeMillis()
                abilityLoopUnsafe {
                    val currentTime: Long = System.currentTimeMillis()
                    if (startTime + this.chargeTime <= currentTime) {
                        charged = true
                    }

                    if (!player.isSneaking) {
                        val factor: Double = if (charged) {
                            this.maxChargeFactor
                        } else {
                            this.maxChargeFactor * (currentTime - startTime) / this.chargeTime
                        }

                        return this.swipe(player, player.eyeLocation, this.damage * factor, this.pushFactor * factor)
                    }

                    if (charged) {
                        player.eyeLocation.spawnParticles(this.particleEffect)
                    }
                }
            }
        }
    }

    private suspend fun swipe(
        source: Player, origin: Location<World>,
        damage: Double = this.damage, pushFactor: Double = this.pushFactor
    ): AbilityResult {
        val raycasts: List<Raycast> = createRaycasts(origin, source.headDirection.normalize())

        val affectedEntities = HashSet<Entity>()
        abilityLoopUnsafe {
            val anySucceeded: Boolean = raycasts.advanceAll {
                if (BuildProtectionService.get().isProtected(source, it)) return@advanceAll AbilityResult.ErrorProtected

                affectEntities(source, affectedEntities, radius) { test: Entity ->
                    with(AirRaycast) {
                        pushEntity(source, test, false, pushFactor, pushFactor)
                    }

                    damageEntity(test, damage)
                }
                playParticles(particleEffect)

                if (Constants.RANDOM.nextInt(4) == 0) {
                    playSounds(SoundTypes.ENTITY_CREEPER_HURT, 0.5, 1.0)
                }

                return@advanceAll Success
            }

            if (!anySucceeded) {
                return Success
            }
        }
    }

    private fun createRaycasts(
        origin: Location<World>, direction: Vector3d
    ): List<Raycast> {
        val result = ArrayList<Raycast>()

        forInclusive(from = -this.arcRadians, to = this.arcRadians, step = this.arcDegreesRadians) { angle: Double ->
            val sinAngle: Double = sin(angle)
            val cosAngle: Double = cos(angle)

            val vx: Double = direction.x * cosAngle - direction.z * sinAngle
            val vz: Double = direction.x * sinAngle + direction.z * cosAngle

            result += Raycast(
                origin = origin,
                direction = direction.withXZ(vx, vz),
                range = this.range,
                speed = this.speed,
                checkDiagonals = true
            )
        }

        return result
    }

}