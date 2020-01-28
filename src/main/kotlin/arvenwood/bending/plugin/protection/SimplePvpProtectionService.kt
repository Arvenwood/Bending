package arvenwood.bending.plugin.protection

import arvenwood.bending.api.protection.PvpProtection
import arvenwood.bending.api.protection.PvpProtectionService
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate

class SimplePvpProtectionService : PvpProtectionService {

    private val protections: Collection<PvpProtection> by lazy(LazyThreadSafetyMode.NONE) {
        Sponge.getRegistry().getAllOf(PvpProtection::class.java)
    }

    override fun isProtected(source: Player, target: Entity): Boolean {
        for (protection: PvpProtection in protections) {
            val value: Tristate = protection.isProtected(source, target)

            if (value != Tristate.UNDEFINED) {
                return value.asBoolean()
            }
        }

        return false
    }
}