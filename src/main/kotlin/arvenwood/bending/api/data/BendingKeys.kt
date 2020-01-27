package arvenwood.bending.api.data

import com.google.common.reflect.TypeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.mutable.Value

object BendingKeys {

    val INVINCIBLE: Key<Value<Boolean>> = Key.builder()
        .type(object : TypeToken<Value<Boolean>>() {})
        .id("bending:invincible")
        .name("Invincible")
        .query(DataQuery.of("Invincible"))
        .build();
}