package pw.dotdash.bending.plugin.protection

import pw.dotdash.bending.api.protection.BuildProtection
import pw.dotdash.bending.api.protection.BuildProtectionService
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

class SimpleBuildProtectionService : BuildProtectionService {

    private val protections: Collection<BuildProtection> by lazy {
        Sponge.getRegistry().getAllOf(BuildProtection::class.java)
    }

    override fun isProtected(source: Player, target: Location<World>): Boolean {
        for (protection: BuildProtection in this.protections) {
            val value: Tristate = protection.isProtected(source, target)

            if (value != Tristate.UNDEFINED) {
                return value.asBoolean()
            }
        }

        return false
    }
}