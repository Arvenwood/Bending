package arvenwood.bending.api.protection

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

interface BuildProtectionService {

    companion object {
        @JvmStatic
        fun get(): BuildProtectionService =
            Sponge.getServiceManager().provideUnchecked(BuildProtectionService::class.java)
    }

    fun isProtected(source: Player, target: Location<World>): Boolean
}