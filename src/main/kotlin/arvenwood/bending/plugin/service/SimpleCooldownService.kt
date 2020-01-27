package arvenwood.bending.plugin.service

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.service.CooldownService
import org.spongepowered.api.entity.living.player.Player
import java.util.*
import kotlin.collections.HashMap

class SimpleCooldownService : CooldownService {

    private val playerMap = HashMap<UUID, MutableMap<AbilityType<*>, Long>>()

    override fun hasCooldown(player: Player, type: AbilityType<*>): Boolean {
        val abilities: MutableMap<AbilityType<*>, Long> = this.playerMap[player.uniqueId] ?: return false
        val current: Long = System.currentTimeMillis()
        val cooldown: Long = abilities[type] ?: return false
        if (cooldown <= current) {
            abilities.remove(type)
            return false
        }
        return true
    }

    override fun removeCooldown(player: Player, type: AbilityType<*>): Long {
        val abilities: MutableMap<AbilityType<*>, Long> = this.playerMap[player.uniqueId] ?: return 0L
        return abilities.remove(type) ?: 0L
    }

    override fun get(player: Player, type: AbilityType<*>): Long {
        val abilities: MutableMap<AbilityType<*>, Long> = this.playerMap[player.uniqueId] ?: return 0L
        return abilities[type] ?: 0L
    }

    override fun set(player: Player, type: AbilityType<*>, value: Long) {
        val abilities: MutableMap<AbilityType<*>, Long> = this.playerMap.computeIfAbsent(player.uniqueId) { HashMap() }
        abilities[type] = System.currentTimeMillis() + value
    }
}