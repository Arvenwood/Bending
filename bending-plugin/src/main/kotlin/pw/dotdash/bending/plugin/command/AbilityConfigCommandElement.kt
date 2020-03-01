package pw.dotdash.bending.plugin.command

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.text.Text
import pw.dotdash.bending.api.ability.AbilityConfig
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.util.unwrap

fun abilityConfig(key: Text, typeKey: Text): CommandElement =
    AbilityConfigCommandElement(key, typeKey)

private class AbilityConfigCommandElement(key: Text, private val typeKey: Text) : CommandElement(key) {

    override fun parse(source: CommandSource, args: CommandArgs, context: CommandContext) {
        val type: AbilityType = context.getOne<AbilityType>(this.typeKey).unwrap()
            ?: throw args.createError(Text.of("Unknown ability type."))
        val configName: String = args.next()

        val config: AbilityConfig = AbilityConfig.get(configName).unwrap()?.takeIf { type in it.loader }
            ?: throw args.createError(Text.of("No config for ability '${type.name}' with name '$configName'"))

        context.putArg(this.untranslatedKey!!, config)
    }

    override fun complete(src: CommandSource, args: CommandArgs, context: CommandContext): List<String> {
        val type: AbilityType = context.getOne<AbilityType>(this.typeKey).unwrap() ?: return emptyList()
        return AbilityConfig.getAll().asSequence().filter { type in it.loader }.map { it.id }.toList()
    }

    override fun parseValue(source: CommandSource, args: CommandArgs): Any? = null
}