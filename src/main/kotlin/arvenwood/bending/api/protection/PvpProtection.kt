package arvenwood.bending.api.protection

import org.spongepowered.api.CatalogType
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate

interface PvpProtection : CatalogType {

    override fun getId(): String

    override fun getName(): String

    fun isProtected(source: Player, target: Entity): Tristate
}