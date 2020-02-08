package arvenwood.bending.api.service

import arvenwood.bending.api.ability.AbilityJob
import org.spongepowered.api.Sponge
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

interface CollisionService {

    companion object {
        @JvmStatic
        fun get(): CollisionService =
            Sponge.getServiceManager().provideUnchecked(CollisionService::class.java)
    }

    operator fun contains(location: Location<World>): Boolean

    operator fun get(location: Location<World>): AbilityJob?

    operator fun set(location: Location<World>, job: AbilityJob?)
}