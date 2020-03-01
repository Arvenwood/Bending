package pw.dotdash.bending.plugin.util

import org.spongepowered.api.data.DataTransactionResult
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.BaseValue

@Suppress("UNCHECKED_CAST")
operator fun <T> DataTransactionResult.get(key: Key<out BaseValue<T>>): T? =
    this.successfulData.find { it.key == key }?.get() as T?