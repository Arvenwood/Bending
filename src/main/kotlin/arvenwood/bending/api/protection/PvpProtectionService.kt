package arvenwood.bending.api.protection

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player

interface PvpProtectionService {

    companion object {
        @JvmStatic
        fun get(): PvpProtectionService =
            Sponge.getServiceManager().provideUnchecked(PvpProtectionService::class.java)
    }

    fun isProtected(source: Player, target: Entity): Boolean
}