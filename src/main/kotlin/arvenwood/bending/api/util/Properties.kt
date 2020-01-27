package arvenwood.bending.api.util

import org.spongepowered.api.data.Property
import org.spongepowered.api.data.property.PropertyHolder

inline fun <reified T : Property<*, *>> PropertyHolder.property(): T? =
    this.getProperty(T::class.java).orElse(null)