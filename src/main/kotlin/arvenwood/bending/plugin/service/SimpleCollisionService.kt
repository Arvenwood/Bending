package arvenwood.bending.plugin.service

import arvenwood.bending.api.service.CollisionService
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

class SimpleCollisionService : CollisionService {

    private val map = HashMap<Location<World>, Int>()

    override fun checkAt(location: Location<World>): Boolean = location in map

    override fun addCollision(location: Location<World>) {
        map.compute(location) { _, counter ->
            if (counter == null) {
                1
            } else {
                counter + 1
            }
        }
    }

    override fun removeCollision(location: Location<World>) {
        map.compute(location) { _, counter ->
            if (counter == null || counter == 1) {
                null
            } else {
                counter - 1
            }
        }
    }
}