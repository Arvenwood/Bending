package arvenwood.bending.plugin.service

import arvenwood.bending.api.Bender
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.plugin.SimpleBender
import org.spongepowered.api.entity.living.player.Player
import java.util.*
import kotlin.collections.HashMap

class SimpleBenderService : BenderService {

    private val benders = HashMap<UUID, Bender>()

    override fun get(player: Player): Bender =
        this.benders.computeIfAbsent(player.uniqueId) { SimpleBender(it) }
}