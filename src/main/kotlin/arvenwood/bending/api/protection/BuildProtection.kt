package arvenwood.bending.api.protection

import org.spongepowered.api.CatalogType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

interface BuildProtection : CatalogType {

    override fun getId(): String

    override fun getName(): String

    fun isProtected(source: Player, target: Location<World>): Tristate
}