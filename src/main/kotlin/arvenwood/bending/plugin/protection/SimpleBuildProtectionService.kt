package arvenwood.bending.plugin.protection

import arvenwood.bending.api.protection.BuildProtection
import arvenwood.bending.api.protection.BuildProtectionService
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

class SimpleBuildProtectionService : BuildProtectionService {

    private val protections: Collection<BuildProtection> by lazy(LazyThreadSafetyMode.NONE) {
        Sponge.getRegistry().getAllOf(BuildProtection::class.java)
    }

    override fun isProtected(source: Player, target: Location<World>): Boolean {
        for (protection: BuildProtection in protections) {
            val value: Tristate = protection.isProtected(source, target)

            if (value != Tristate.UNDEFINED) {
                return value.asBoolean()
            }
        }

        return false
    }
}