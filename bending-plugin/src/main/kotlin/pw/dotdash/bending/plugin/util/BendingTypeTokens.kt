package pw.dotdash.bending.plugin.util

import com.google.common.reflect.TypeToken
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.ability.AbilityExecutionType
import pw.dotdash.bending.api.bender.Bender

object BendingTypeTokens {

    val AFFECTED_LOCATIONS: TypeToken<Collection<Location<World>>> = typeToken()

    val AFFECTED_ENTITIES: TypeToken<Collection<Entity>> = typeToken()

    val BENDER_TOKEN: TypeToken<Bender> = typeToken()

    val ABILITY_EXECUTION_TYPE_TOKEN: TypeToken<AbilityExecutionType> = typeToken()

    val LOCATION_WORLD_TOKEN: TypeToken<Location<World>> = typeToken()

    val PLAYER_TOKEN: TypeToken<Player> = typeToken()

    private inline fun <reified T> typeToken(): TypeToken<T> = object : TypeToken<T>() {}
}