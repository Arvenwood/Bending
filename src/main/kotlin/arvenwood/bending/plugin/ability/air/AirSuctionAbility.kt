package arvenwood.bending.plugin.ability.air

import arvenwood.bending.api.ability.*
import arvenwood.bending.api.ability.AbilityResult.ErrorDied
import arvenwood.bending.api.ability.AbilityResult.Success
import arvenwood.bending.api.ability.StandardContext.currentLocation
import arvenwood.bending.api.ability.StandardContext.direction
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.isNearDiagonalWall
import arvenwood.bending.api.util.spawnParticles
import arvenwood.bending.plugin.Constants
import arvenwood.bending.plugin.ability.AbilityTypes
import com.flowpowered.math.vector.Vector3d
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

/**
 * TODO make it work
 */
data class AirSuctionAbility(
    override val cooldown: Long,
    val pushFactor: Double,
    val radius: Double,
    val range: Double,
    val speed: Double,
    val numParticles: Int
) : Ability<AirSuctionAbility> {
    constructor(node: ConfigurationNode) : this(
        cooldown = node.getNode("cooldown").long,
        pushFactor = node.getNode("pushFactor").double,
        radius = node.getNode("radius").double,
        range = node.getNode("range").double,
        speed = node.getNode("speed").double,
        numParticles = node.getNode("numParticles").int
    )

    override val type: AbilityType<AirSuctionAbility> = AbilityTypes.AIR_SUCTION

    private val speedFactor: Double = this.speed * (50.0 / 1000.0)

    override suspend fun execute(context: AbilityContext, executionType: AbilityExecutionType): AbilityResult {
        val player: Player = context.require(StandardContext.player)

        var location: Location<World> by context.by(currentLocation)
        val direction: Vector3d = context.require(direction)

        abilityLoopUnsafe {
            if (player.isRemoved) return ErrorDied

            // Show the particles.
            location.spawnParticles(EffectService.get().createRandomParticle(Elements.AIR, this.numParticles))
            if (Constants.RANDOM.nextInt(4) == 0) {
                // Play the sounds every now and then.
                location.extent.playSound(SoundTypes.ENTITY_CREEPER_HURT, location.position, 0.5, 1.0)
            }

            if (location.isNearDiagonalWall(direction)) {
                // Stop if we've hit a diagonal wall.
                return Success
            }

            location = location.add(direction.mul(this.speedFactor))
        }
    }

    private fun advance(location: Location<World>, direction: Vector3d): Location<World> {
        return location.add(direction.mul(this.speedFactor))
    }
}