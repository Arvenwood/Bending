package arvenwood.bending.plugin.service

import arvenwood.bending.api.service.ProtectionService
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object EmptyProtectionService : ProtectionService {

    override fun isProtected(player: Player, location: Location<World>): Boolean = false
}