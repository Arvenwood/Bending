package arvenwood.bending.api.config.simple

import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigLoader
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import org.slf4j.LoggerFactory
import org.spongepowered.api.Sponge
import java.nio.file.Files
import java.nio.file.Path

data class FileAbilityConfigLoader(val file: Path) : AbilityConfigLoader {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("FileAbilityConfigLoader")
    }

    init {
        require(Files.isRegularFile(this.file)) { "must be a regular file: $file" }
    }

    private val name: String =
        this.file.fileName.toString().substringBefore(".conf")

    private val loader: HoconConfigurationLoader =
        HoconConfigurationLoader.builder().setPath(this.file).build()

    override fun load(): List<AbilityConfig> {
        if (Files.readAllLines(this.file).size > 0) {
            println("File has text: $file")
        }

        val result = ArrayList<AbilityConfig>()
        for ((key: Any, node: CommentedConfigurationNode) in this.loader.load().childrenMap) {
            val type: AbilityType<*>? = Sponge.getRegistry()
                .getType(AbilityType::class.java, key.toString())
                .orElse(null)

            if (type == null) {
                LOGGER.info("Unrecognized ability: $key")
                continue
            }

            result += SimpleAbilityConfig(this.name, type, node)
        }
        return result
    }
}