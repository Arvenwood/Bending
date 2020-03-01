package pw.dotdash.bending.plugin.bender

import org.spongepowered.api.entity.living.player.Player
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.bender.BenderService
import java.util.*
import kotlin.collections.HashMap

class SimpleBenderService : BenderService {

    private val benders = HashMap<UUID, Bender>()

    override fun getBender(uniqueId: UUID): Optional<Bender> =
        Optional.ofNullable(this.benders[uniqueId])

    override fun getOrCreateBender(player: Player): Bender =
        this.benders.computeIfAbsent(player.uniqueId) { SimpleBender(it) }
}