package arvenwood.bending.plugin.command

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfigService
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.text.Text

fun abilityConfig(typeKey: Text, nameKey: Text): CommandElement =
    AbilityConfigCommandElement(typeKey, nameKey)

private class AbilityConfigCommandElement(private val typeKey: Text, private val nameKey: Text) : CommandElement(typeKey) {

    override fun parseValue(source: CommandSource, args: CommandArgs): Any? {
        val type: AbilityType<*> = Sponge.getRegistry().getType(AbilityType::class.java, args.next()).orElse(null)
            ?: throw args.createError(Text.of("Unknown ability type"))
        val name: String = args.nextIfPresent().orElse("default")

        return AbilityConfigService.get()[name, type]
            ?: throw args.createError(Text.of("Unknown ability config '$name' for ability '${type.name}'"))
    }

    override fun complete(src: CommandSource, args: CommandArgs, context: CommandContext): List<String> {
        val next: String = args.nextIfPresent().orElse("")
        if (next.isNotBlank()) {
            println("all configs")
            // Return all configs for this ability.
            val type: AbilityType<*> = Sponge.getRegistry().getType(AbilityType::class.java, next).orElse(null)
                ?: return emptyList()
            return AbilityConfigService.get()[type].keys.toList()
        } else {
            println("all abilities")
            // Otherwise return all registered abilities.
            return Sponge.getRegistry().getAllOf(AbilityType::class.java).map { it.id }
        }
    }

    override fun getUsage(src: CommandSource): Text {
        return Text.of(this.typeKey, " ", this.nameKey)
    }
}