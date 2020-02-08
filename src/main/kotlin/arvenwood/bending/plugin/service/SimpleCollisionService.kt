package arvenwood.bending.plugin.service

import arvenwood.bending.api.ability.AbilityJob
import arvenwood.bending.api.service.CollisionService
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

class SimpleCollisionService : CollisionService {

    private val locations = HashMap<Location<World>, AbilityJob>()

    override fun contains(location: Location<World>): Boolean = location in this.locations

    override fun get(location: Location<World>): AbilityJob? = this.locations[location]

    override fun set(location: Location<World>, job: AbilityJob?) {
        if (job != null) {
            this.locations[location] = job
        } else {
            this.locations.remove(location)
        }
    }
}