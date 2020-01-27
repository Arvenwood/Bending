package arvenwood.bending.api.service

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

interface ProtectionService {

    companion object {
        @JvmStatic
        fun get(): ProtectionService =
            Sponge.getServiceManager().provideUnchecked(ProtectionService::class.java)
    }

    fun isProtected(player: Player, location: Location<World>): Boolean
}