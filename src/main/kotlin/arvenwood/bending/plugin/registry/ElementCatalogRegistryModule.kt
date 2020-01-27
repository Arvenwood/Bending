package arvenwood.bending.plugin.registry

import arvenwood.bending.api.element.Element
import arvenwood.bending.api.element.Elements

class ElementCatalogRegistryModule : HashMapCatalogRegistryModule<Element>() {
    init {
        this.registerAdditionalCatalog(Elements.Air)
        this.registerAdditionalCatalog(Elements.Fire)
    }
}