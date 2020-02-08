package arvenwood.bending.plugin.util

import kotlin.random.Random

fun Random.nextAngleRad(): Double =
    this.nextDouble() * 2 * Math.PI