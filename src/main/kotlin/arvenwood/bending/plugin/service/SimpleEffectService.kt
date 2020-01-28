package arvenwood.bending.plugin.service

import arvenwood.bending.api.element.Element
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.util.identityHashMapOf
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.effect.particle.ParticleTypes
import java.util.*

class SimpleEffectService : EffectService {

    private val elementParticles: IdentityHashMap<Elements.Air, ParticleType> = identityHashMapOf(
        Elements.Air to ParticleTypes.CLOUD
    )

    override fun getParticleType(element: Element): ParticleType =
        requireNotNull(this.elementParticles[element]) { "Unregistered element: $element" }

    override fun createParticle(element: Element, quantity: Int, offset: Vector3d): ParticleEffect =
        ParticleEffect.builder()
            .type(getParticleType(element))
            .quantity(quantity)
            .offset(offset)
            .build()

    override fun createRandomParticle(element: Element, quantity: Int): ParticleEffect =
        ParticleEffect.builder()
            .type(getParticleType(element))
            .quantity(quantity)
            .offset(Vector3d(Math.random(), Math.random(), Math.random()))
            .build()
}