package arvenwood.bending.api.config.simple

import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class FolderAbilityConfigLoader(private val folder: Path) : AbilityConfigLoader {
    init {
        require(Files.isDirectory(this.folder)) { "must be a folder: $folder" }
    }

    override fun load(): List<AbilityConfig> =
        Files.list(this.folder).asSequence()
            .flatMap { FileAbilityConfigLoader(it).load().asSequence() }
            .toList()
}