package arvenwood.bending.api.ability

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

@Suppress("ClassName")
object StandardContext {

    /**
     * The player that initiated the ability.
     */
    object player : AbilityContext.Key<Player>("bending:player", "Player Context")

    /**
     * How the ability was initiated.
     */
    object executionType : AbilityContext.Key<AbilityExecutionType>("bending:execution_type", "Execution Type Context")

    /**
     * Where the ability was started.
     */
    object origin : AbilityContext.Key<Location<World>>("bending:origin", "Origin Location Context")

    /**
     * Where the ability currently is.
     */
    object currentLocation : AbilityContext.Key<Location<World>>("bending:current_location", "Current Location Context")

    /**
     * The direction that the ability is heading.
     */
    object direction : AbilityContext.Key<Vector3d>("bending:direction", "Direction Context")

    /**
     * Locations that have been affected by the ability.
     */
    object affectedLocations : AbilityContext.Key<MutableCollection<Location<World>>>("bending:affected_locations", "Affected Locations Context")

    /**
     * Entities that have been affected by the ability.
     */
    object affectedEntities : AbilityContext.Key<MutableCollection<Entity>>("bending:affected_entities", "Affected Entities Context")

    object height : AbilityContext.Key<Double>("bending:height", "Height Context")

    object radius : AbilityContext.Key<Double>("bending:radius", "Radius Context")

    object fallDistance: AbilityContext.Key<Float>("bending:fall_distance", "Fall Distance Context")
}