package arvenwood.bending.plugin.command

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigService
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.text.Text

fun abilityConfig(key: Text, typeKey: Text): CommandElement =
    AbilityConfigCommandElement(key, typeKey)

private class AbilityConfigCommandElement(key: Text, private val typeKey: Text) : CommandElement(key) {

    override fun parse(source: CommandSource, args: CommandArgs, context: CommandContext) {
        val type: AbilityType<*> = context.getOne<AbilityType<*>>(typeKey).orElse(null) ?: return
        val configName: String = args.next()

        val config: AbilityConfig = AbilityConfigService.get()[configName, type]
            ?: throw args.createError(Text.of("No config for ability '${type.name}' with name '$configName'"))

        context.putArg(this.untranslatedKey!!, config)
    }

    override fun complete(src: CommandSource, args: CommandArgs, context: CommandContext): List<String> {
        val type: AbilityType<*> = context.getOne<AbilityType<*>>(typeKey).orElse(null) ?: return emptyList()

        return AbilityConfigService.get()[type].keys.toList()
    }

    override fun parseValue(source: CommandSource, args: CommandArgs): Any? = null
}