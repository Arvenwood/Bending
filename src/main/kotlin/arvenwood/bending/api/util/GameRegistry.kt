package arvenwood.bending.api.util

import org.spongepowered.api.CatalogType
import org.spongepowered.api.GameRegistry
import org.spongepowered.api.registry.CatalogRegistryModule
import org.spongepowered.api.util.ResettableBuilder

inline fun <reified B> GameRegistry.registerBuilderSupplier(noinline supplier: () -> B): GameRegistry =
    this.registerBuilderSupplier(B::class.java, supplier)

inline fun <reified T : CatalogType> GameRegistry.registerModule(module: CatalogRegistryModule<T>): GameRegistry =
    this.registerModule(T::class.java, module)