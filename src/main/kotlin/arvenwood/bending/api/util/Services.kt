package arvenwood.bending.api.util

import org.spongepowered.api.service.ServiceManager

inline fun <reified T> ServiceManager.setProvider(plugin: Any, provider: T) =
    this.setProvider(plugin, T::class.java, provider)