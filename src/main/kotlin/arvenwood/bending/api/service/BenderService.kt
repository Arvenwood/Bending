package arvenwood.bending.api.service

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecution
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import kotlinx.coroutines.Deferred
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player

interface BenderService {

    companion object {
        @JvmStatic
        fun get(): BenderService =
            Sponge.getServiceManager().provideUnchecked(BenderService::class.java)
    }

    operator fun get(player: Player): Bender
}