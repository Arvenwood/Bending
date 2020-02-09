package arvenwood.bending.api.util.catalog

import org.spongepowered.api.CatalogType
import org.spongepowered.api.Sponge
import org.spongepowered.api.util.ResettableBuilder

abstract class SimpleCatalogTypeManager<T : CatalogType, B : ResettableBuilder<T, B>>(
    private val typeClass: Class<T>,
    private val builderClass: Class<B>
) : CatalogTypeManager<T, B> {

    override fun builder(): B = Sponge.getRegistry().createBuilder(this.builderClass)

    override operator fun get(id: String): T? = Sponge.getRegistry().getType(this.typeClass, id).orElse(null)

    override val all: Collection<T> = Sponge.getRegistry().getAllOf(this.typeClass)
}