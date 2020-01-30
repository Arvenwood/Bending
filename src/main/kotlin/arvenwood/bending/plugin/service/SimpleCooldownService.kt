package arvenwood.bending.plugin.service

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.service.CooldownService
import com.google.common.collect.Table
import com.google.common.collect.Tables
import org.spongepowered.api.entity.living.player.Player
import java.util.*
import kotlin.collections.HashMap

class SimpleCooldownService : CooldownService {

    private val playerMap: Table<UUID, AbilityType<*>, Long> =
        Tables.newCustomTable<UUID, AbilityType<*>, Long>(HashMap()) { IdentityHashMap() }

    override fun hasCooldown(player: Player, type: AbilityType<*>): Boolean {
        val cooldown: Long = this.playerMap[player.uniqueId, type] ?: return false
        val current: Long = System.currentTimeMillis()
        if (cooldown <= current) {
            this.playerMap.remove(player.uniqueId, type)
            return false
        }
        return true
    }

    override fun removeCooldown(player: Player, type: AbilityType<*>): Long =
        this.playerMap.remove(player.uniqueId, type) ?: 0L

    override fun get(player: Player, type: AbilityType<*>): Long =
        this.playerMap[player.uniqueId, type] ?: 0L

    override fun set(player: Player, type: AbilityType<*>, value: Long) {
        this.playerMap.put(player.uniqueId, type, value)
    }
}