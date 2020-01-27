package arvenwood.bending.api.service

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityType
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player

interface CooldownService {

    companion object {
        @JvmStatic
        fun get(): CooldownService =
            Sponge.getServiceManager().provideUnchecked(CooldownService::class.java)
    }

    fun hasCooldown(player: Player, type: AbilityType<Ability<*>>): Boolean

    fun removeCooldown(player: Player, type: AbilityType<Ability<*>>): Long

    operator fun get(player: Player, type: AbilityType<Ability<*>>): Long

    operator fun set(player: Player, type: AbilityType<Ability<*>>, value: Long)
}