package arvenwood.bending.plugin

import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityExecutionTypes
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Element
import arvenwood.bending.api.element.Elements
import arvenwood.bending.plugin.ability.AbilityTypes
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent

object CatalogRegistration {

    @Listener
    fun onRegisterAbilityExecutionType(event: GameRegistryEvent.Register<AbilityExecutionType>) {
        Bending.LOGGER.info("Registering ability execution types...")

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
        Bending.LOGGER.info("Registering classical elements and chi...")

        event.register(Elements.WATER)
        event.register(Elements.EARTH)
        event.register(Elements.FIRE)
        event.register(Elements.AIR)

        event.register(Elements.CHI)
    }

    @Listener
    fun onRegisterAbilityType(event: GameRegistryEvent.Register<AbilityType<*>>) {
        Bending.LOGGER.info("Registering builtin abilities...")

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
}