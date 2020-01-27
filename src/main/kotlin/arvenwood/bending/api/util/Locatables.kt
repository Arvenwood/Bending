package arvenwood.bending.api.util

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.world.Locatable

inline val Locatable.position: Vector3d get() = this.location.position