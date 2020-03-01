package pw.dotdash.bending.plugin.protection

import pw.dotdash.bending.api.protection.PvpProtection
import pw.dotdash.bending.api.protection.PvpProtectionService
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate

class SimplePvpProtectionService : PvpProtectionService {

    private val protections: Collection<PvpProtection> by lazy {
        Sponge.getRegistry().getAllOf(PvpProtection::class.java)
    }

    override fun isProtected(source: Player, target: Entity): Boolean {
        for (protection: PvpProtection in this.protections) {
            val value: Tristate = protection.isProtected(source, target)

            if (value != Tristate.UNDEFINED) {
                return value.asBoolean()
            }
        }

        return false
    }
}