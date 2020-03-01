package pw.dotdash.bending.api.ability.config

import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import pw.dotdash.bending.api.ability.Ability
import pw.dotdash.bending.api.ability.AbilityConfigLoader
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.util.unwrap
import java.io.IOException
import java.nio.file.Path
import java.util.*

class ConfigFileLoader(file: Path) : AbilityConfigLoader {

    private val loader: HoconConfigurationLoader =
        HoconConfigurationLoader.builder()
            .setPath(file)
            .build()

    private val abilityMap = HashMap<AbilityType, Ability>()

    init {
        val node: ConfigurationNode = try {
            this.loader.load()
        } catch (e: IOException) {
            e.printStackTrace()
            this.loader.createEmptyNode()
        }

        for ((key: Any, child: ConfigurationNode) in node.childrenMap) {
            val type: AbilityType? = AbilityType.get(key.toString()).unwrap()
            if (type == null) {
                println("Unknown ability type $key in config file $file")
                continue
            }
            this.abilityMap[type] = type.load(child).get()
        }
    }

    override fun getAbilityTypes(): Collection<AbilityType> = this.abilityMap.keys

    override fun load(type: AbilityType): Optional<Ability> =
        Optional.ofNullable(this.abilityMap[type])

    override fun contains(type: AbilityType): Boolean =
        type in this.abilityMap
}