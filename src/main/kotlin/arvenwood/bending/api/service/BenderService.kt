package arvenwood.bending.api.service

import arvenwood.bending.api.Bender
import org.spongepowered.api.Sponge
import java.util.*

interface BenderService {

    companion object {
        @JvmStatic
        fun get(): BenderService =
            Sponge.getServiceManager().provideUnchecked(BenderService::class.java)
    }

    /**
     * Gets a [Bender] by their [UUID].
     *
     * @param uniqueId The UUID to get the bender from
     * @return The [Bender]
     */
    operator fun get(uniqueId: UUID): Bender
}