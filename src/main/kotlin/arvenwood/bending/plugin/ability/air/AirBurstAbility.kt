package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityExecutionType.Fall
import arvenwood.bending.api.ability.AbilityExecutionType.Sneak
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.eyeLocation
import arvenwood.bending.api.util.isSneaking
import arvenwood.bending.api.util.spawnParticles
import arvenwood.bending.plugin.action.ParticleProjectile
import arvenwood.bending.plugin.action.advanceAll
import arvenwood.bending.plugin.util.whileExclusive
import arvenwood.bending.plugin.util.whileInclusive
import com.flowpowered.math.vector.Vector3d
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import kotlin.math.cos
import kotlin.math.sin

data class AirBurstAbility(
    override val cooldown: Long,
    val chargeTime: Long,
    val damage: Double,
    val pushFactor: Double,
    val blastRadius: Double,
    val range: Double,
    val speed: Double,
    val isFallBurst: Boolean,
    val fallThreshold: Double,
    val numSneakParticles: Int,
    val angleTheta: Double,
    val anglePhi: Double
) : Ability<AirBurstAbility> {

    override val type: AbilityType<AirBurstAbility>
        get() = TODO("not implemented")

    companion object : AbstractAbilityType<AirBurstAbility>(
        element = Elements.Air,
        executionTypes = setOf(Sneak::class, Fall::class),
        id = "bending:air_burst",
        name = "AirBurst"
    ) {
        override val default: Ability<AirBurstAbility>
            get() = TODO("not implemented")

        override fun load(node: ConfigurationNode): AirBurstAbility {
            TODO("not implemented")
        }
    }

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)
        val origin: Location<World> = context.require(StandardContext.origin)

        if (executionType is Fall) {
            if (executionType.damage >= this.fallThreshold) {
                return this.fallBurst(origin)
            }
            return Success
        }

        var charged = false

        abilityLoopUnsafe {
            if (!player.isSneaking) {
                return if (charged) this.sphereBurst() else Success
            }

            if (charged) {
                val location: Location<World> = player.eyeLocation
                location.spawnParticles(EffectService.get().createRandomParticle(Elements.Air, this.numSneakParticles))
            }
        }
    }

    private suspend fun fallBurst(origin: Location<World>): AbilityResult {
        val projectiles: List<ParticleProjectile> = createProjectiles(origin, 75.0, 105.0)

        abilityLoopUnsafe {
            val anySucceeded: Boolean = projectiles.advanceAll {

            }

            if (!anySucceeded) {
                return Success
            }
        }
    }

    private suspend fun sphereBurst(): AbilityResult {
        abilityLoopUnsafe {

        }
    }

    private fun createProjectiles(origin: Location<World>, thetaMin: Double, thetaMax: Double): List<ParticleProjectile> {
        val result = ArrayList<ParticleProjectile>()

        whileInclusive(from = thetaMin, to = thetaMax, step = this.angleTheta) { theta: Double ->
            val sinTheta: Double = sin(Math.toRadians(theta))
            val cosTheta: Double = cos(Math.toRadians(theta))

            val deltaPhi: Double = this.anglePhi / sin(Math.toRadians(theta))
            whileExclusive(from = 0.0, to = 360.0, step = deltaPhi) { phi: Double ->
                val sinPhi: Double = sin(Math.toRadians(phi))
                val cosPhi: Double = cos(Math.toRadians(phi))

                val x: Double = cosPhi * sinTheta
                val y: Double = sinPhi * sinTheta
                val z: Double = cosTheta

                val direction = Vector3d(x, y, z)
                result += ParticleProjectile(origin, direction, this.speed, this.range, checkDiagonals = true)
            }
        }

        return result
    }
}