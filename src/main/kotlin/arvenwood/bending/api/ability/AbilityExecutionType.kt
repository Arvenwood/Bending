package arvenwood.bending.api.ability

import arvenwood.bending.api.util.catalog.CatalogTypeManager
import arvenwood.bending.api.util.catalog.catalogTypeManager
import org.spongepowered.api.CatalogType
import org.spongepowered.api.util.ResettableBuilder
import kotlin.coroutines.CoroutineContext

interface AbilityExecutionType : CatalogType, CoroutineContext.Element {

    override val key: CoroutineContext.Key<AbilityExecutionType> get() = AbilityExecutionType

    companion object :
        CatalogTypeManager<AbilityExecutionType, Builder> by catalogTypeManager(),
        CoroutineContext.Key<AbilityExecutionType>

    override fun getId(): String

    override fun getName(): String

    interface Builder : ResettableBuilder<AbilityExecutionType, Builder> {

        fun id(id: String): Builder

        fun name(name: String): Builder

        fun build(): AbilityExecutionType
    }
}