package pw.dotdash.bending.plugin.feature

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer

abstract class Feature(owner: PluginContainer, pluginId: String) {
    protected val logger: Logger = LoggerFactory.getLogger("${owner.id}/$pluginId")

    fun register(plugin: Any) {
        Sponge.getEventManager().registerListeners(plugin, this)
    }
}