package arvenwood.bending.api.util

import org.spongepowered.api.event.cause.Cause

inline fun <reified T> Cause.first(): T? =
    this.first(T::class.java).orElse(null)

inline fun <reified T> Cause.last(): T? =
    this.last(T::class.java).orElse(null)