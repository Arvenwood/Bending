package arvenwood.bending.api.util

import org.spongepowered.api.CatalogType
import org.spongepowered.api.GameRegistry
import org.spongepowered.api.registry.CatalogRegistryModule

inline fun <reified T : CatalogType> GameRegistry.registerModule(module: CatalogRegistryModule<T>) {
    this.registerModule(T::class.java, module)
}