package arvenwood.bending.api.service

import org.spongepowered.api.Sponge
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

interface AbilityCollisionService {

    companion object {
        @JvmStatic
        fun get(): AbilityCollisionService =
            Sponge.getServiceManager().provideUnchecked(AbilityCollisionService::class.java)
    }

    fun checkAt(location: Location<World>): Boolean

    fun addCollision(location: Location<World>)

    fun removeCollision(location: Location<World>)
}