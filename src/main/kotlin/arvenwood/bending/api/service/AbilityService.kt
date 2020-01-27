package arvenwood.bending.api.service

import org.spongepowered.api.Sponge

interface AbilityService {

    companion object {
        @JvmStatic
        fun get(): AbilityService =
            Sponge.getServiceManager().provideUnchecked(AbilityService::class.java)
    }

    val timeStep: Long

    val abilityDelayMilli: Long
}