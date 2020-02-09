package arvenwood.bending.api.util.catalog

import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.ResettableBuilder

inline fun <reified T : CatalogType, reified B : ResettableBuilder<T, B>> catalogTypeManager(): CatalogTypeManager<T, B> =
    object : SimpleCatalogTypeManager<T, B>(T::class.java, B::class.java) {}

interface CatalogTypeManager<T : CatalogType, B : ResettableBuilder<T, B>> {

    fun builder(): B

    operator fun get(id: String): T?

    val all: Collection<T>
}