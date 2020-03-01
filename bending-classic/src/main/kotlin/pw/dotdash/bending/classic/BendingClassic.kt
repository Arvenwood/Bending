package pw.dotdash.bending.classic

import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.classic.ability.ClassicAbilityTypes

@Plugin(
    id = "bending-classic", name = "Bending Classic", version = "0.1.0",
    description = "Classic bending abilities with modern performance.",
    url = "https://ore.spongepowered.org/doot/BendingClassic",
    dependencies = [Dependency(id = "bending")],
    authors = ["doot"]
)
class BendingClassic @Inject constructor(private val logger: Logger) {

    @Listener
    fun onRegisterAbility(event: GameRegistryEvent.Register<AbilityType>) {
        this.logger.info("Registering classic abilities...")

        event.register(ClassicAbilityTypes.AIR_AGILITY)
        event.register(ClassicAbilityTypes.AIR_BLAST)
        event.register(ClassicAbilityTypes.AIR_BURST)
        event.register(ClassicAbilityTypes.AIR_JUMP)
        event.register(ClassicAbilityTypes.AIR_SCOOTER)
        event.register(ClassicAbilityTypes.AIR_SHIELD)
        event.register(ClassicAbilityTypes.AIR_SPOUT)
        event.register(ClassicAbilityTypes.AIR_SUCTION)
        event.register(ClassicAbilityTypes.AIR_SWIPE)
        event.register(ClassicAbilityTypes.AIR_TORNADO)

        event.register(ClassicAbilityTypes.FIRE_BLAST)
        event.register(ClassicAbilityTypes.FIRE_COMBUSTION)
        event.register(ClassicAbilityTypes.FIRE_JET)
        event.register(ClassicAbilityTypes.FIRE_SHIELD)
        event.register(ClassicAbilityTypes.FIRE_WALL)
    }
}