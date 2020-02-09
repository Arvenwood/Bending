package arvenwood.bending.plugin

import kotlin.random.Random
import kotlin.random.asKotlinRandom

object Constants {

    @JvmField
    val RANDOM: Random = java.util.Random().asKotlinRandom()
}