package arvenwood.bending.plugin.feature

import org.slf4j.Logger
import org.spongepowered.api.Sponge

class FeatureManager {

    private val featureSuppliers = HashMap<String, () -> () -> Feature>()

    fun addFeature(pluginId: String, supplier: () -> () -> Feature) {
        this.featureSuppliers[pluginId] = supplier
    }

    fun register(plugin: Any, logger: Logger) {
        for ((pluginId: String, supplier: () -> () -> Feature) in this.featureSuppliers) {
            if (Sponge.getPluginManager().isLoaded(pluginId)) {
                logger.info("Plugin $pluginId found. Enabling integrations...")
                supplier()().register(plugin)
            }
        }
    }
}