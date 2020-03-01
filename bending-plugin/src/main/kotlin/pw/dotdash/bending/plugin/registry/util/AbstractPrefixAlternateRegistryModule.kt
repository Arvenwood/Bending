package pw.dotdash.bending.plugin.registry.util

import org.spongepowered.api.CatalogType
import org.spongepowered.api.registry.AlternateCatalogRegistryModule
import java.util.*
import kotlin.collections.HashMap

abstract class AbstractPrefixAlternateRegistryModule<T : CatalogType>(defaultNamespace: String) :
    AbstractPrefixCheckRegistryModule<T>(defaultNamespace), AlternateCatalogRegistryModule<T> {

    override fun provideCatalogMap(): MutableMap<String, T> {
        val result = HashMap<String, T>()

        for ((id: String, value: T) in this.catalogMap) {
            result[id.replace("$defaultNamespace:", "")] = value
            result[value.name.toLowerCase(Locale.ENGLISH)] = value
        }

        return result
    }
}