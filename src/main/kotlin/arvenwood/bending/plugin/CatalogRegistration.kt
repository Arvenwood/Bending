package arvenwood.bending.plugin

import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityExecutionTypes
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Element
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtection
import arvenwood.bending.api.protection.PvpProtection
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.protection.GriefDefenderProtection
import com.griefdefender.api.GriefDefender
import com.griefdefender.api.permission.flag.Flags
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent

object CatalogRegistration {

    @Listener
    fun onRegisterAbilityExecutionType(event: GameRegistryEvent.Register<AbilityExecutionType>) {
        event.register(AbilityExecutionTypes.COMBO)
        event.register(AbilityExecutionTypes.FALL)
        event.register(AbilityExecutionTypes.JUMP)
        event.register(AbilityExecutionTypes.LEFT_CLICK)
        event.register(AbilityExecutionTypes.RIGHT_CLICK)
        event.register(AbilityExecutionTypes.SNEAK)
        event.register(AbilityExecutionTypes.SPRINT_OFF)
        event.register(AbilityExecutionTypes.SPRINT_ON)
        event.register(AbilityExecutionTypes.SWAP_HAND)
    }

    @Listener
    fun onRegisterElement(event: GameRegistryEvent.Register<Element>) {
        // Register the classical elements.
        event.register(Elements.WATER)
        event.register(Elements.EARTH)
        event.register(Elements.FIRE)
        event.register(Elements.AIR)

        // ...and Chi.
        event.register(Elements.CHI)
    }

    @Listener
    fun onRegisterAbilityType(event: GameRegistryEvent.Register<AbilityType<*>>) {
        // Register the builtin abilities.

        event.register(AbilityTypes.AIR_AGILITY)
        event.register(AbilityTypes.AIR_BLAST)
        event.register(AbilityTypes.AIR_BURST)
        event.register(AbilityTypes.AIR_JUMP)
        event.register(AbilityTypes.AIR_SCOOTER)
        event.register(AbilityTypes.AIR_SHIELD)
        event.register(AbilityTypes.AIR_SPOUT)
        event.register(AbilityTypes.AIR_SWIPE)
        event.register(AbilityTypes.AIR_TORNADO)

        event.register(AbilityTypes.EARTH_TUNNEL)

        event.register(AbilityTypes.FIRE_BLAST)
        event.register(AbilityTypes.FIRE_COMBUSTION)
        event.register(AbilityTypes.FIRE_JET)
        event.register(AbilityTypes.FIRE_SHIELD)
        event.register(AbilityTypes.FIRE_WALL)
    }

    @Listener
    fun onRegisterBuildProtection(event: GameRegistryEvent.Register<BuildProtection>) {
        // Register GriefDefender protection if found.
        val griefDefender: GriefDefenderProtection? = GriefDefenderProtection.load()
        if (griefDefender != null) {
            if (GriefDefender.getCore().isProtectionModuleEnabled(Flags.BLOCK_PLACE)
                || GriefDefender.getCore().isProtectionModuleEnabled(Flags.BLOCK_BREAK)) {
                Bending.LOGGER.info("GriefDefender found. Enabling build protection...")
                event.register(griefDefender)
            }
        }
    }

    @Listener
    fun onRegisterPvpProtection(event: GameRegistryEvent.Register<PvpProtection>) {
        // Register GriefDefender protection if found.
        val griefDefender: GriefDefenderProtection? = GriefDefenderProtection.load()
        if (griefDefender != null) {
            if (GriefDefender.getCore().isProtectionModuleEnabled(Flags.ENTITY_DAMAGE)) {
                Bending.LOGGER.info("GriefDefender found. Enabling pvp protection...")
                event.register(griefDefender)
            }
        }
    }
}