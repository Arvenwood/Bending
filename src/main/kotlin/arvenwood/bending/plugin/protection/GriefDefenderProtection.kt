package arvenwood.bending.plugin.protection

import arvenwood.bending.api.protection.BuildProtection
import arvenwood.bending.api.protection.PvpProtection
import com.griefdefender.api.GriefDefender
import com.griefdefender.api.Subject
import com.griefdefender.api.claim.Claim
import com.griefdefender.api.permission.flag.Flags
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object GriefDefenderProtection : BuildProtection, PvpProtection {

    fun load(): GriefDefenderProtection? = this.takeIf { Sponge.getPluginManager().isLoaded("griefdefender") }

    override fun getId(): String = "bending:griefdefender"

    override fun getName(): String = "GriefDefender Protection"

    override fun isProtected(source: Player, target: Location<World>): Tristate {
        val claim: Claim = GriefDefender.getCore().getClaimManager(target.extent.uniqueId)
            .getClaimAt(target.blockPosition)
        val subject: Subject = GriefDefender.getCore().getSubject(source.uniqueId.toString())

        return claim.getActiveFlagPermissionValue(Flags.BLOCK_BREAK, subject, source, null, emptySet(), true).toSponge()
            .and(claim.getActiveFlagPermissionValue(Flags.BLOCK_PLACE, subject, source, null, emptySet(), true).toSponge())
    }

    override fun isProtected(source: Player, target: Entity): Tristate {
        val claim: Claim = GriefDefender.getCore().getClaimManager(target.world.uniqueId)
            .getClaimAt(target.location.blockPosition)
        val subject: Subject = GriefDefender.getCore().getSubject(source.uniqueId.toString())

        return claim.getActiveFlagPermissionValue(Flags.ENTITY_DAMAGE, subject, source, target, emptySet()).toSponge()
    }

    private fun com.griefdefender.api.Tristate.toSponge(): Tristate =
        when (this) {
            com.griefdefender.api.Tristate.TRUE -> Tristate.TRUE
            com.griefdefender.api.Tristate.FALSE -> Tristate.FALSE
            com.griefdefender.api.Tristate.UNDEFINED -> Tristate.UNDEFINED
        }
}