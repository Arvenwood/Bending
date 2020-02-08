package arvenwood.bending.plugin.service

import arvenwood.bending.api.Bender
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.plugin.SimpleBender
import java.util.*
import kotlin.collections.HashMap

class SimpleBenderService : BenderService {

    private val benders = HashMap<UUID, Bender>()

    override fun get(uniqueId: UUID): Bender =
        this.benders.computeIfAbsent(uniqueId) { SimpleBender(it) }
}