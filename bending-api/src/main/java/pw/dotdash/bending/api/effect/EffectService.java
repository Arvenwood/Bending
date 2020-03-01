package pw.dotdash.bending.api.effect;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import pw.dotdash.bending.api.element.Element;

/**
 * Represents a service used for the creation of particle effects.
 */
public interface EffectService {

    /**
     * Gets the singleton instance of the {@link EffectService}.
     *
     * @return The singleton service instance
     */
    static EffectService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(EffectService.class);
    }

    ParticleEffect getExtinguishEffect();

    ParticleEffect createParticle(Element element, int quantity, Vector3d offset);

    ParticleEffect createRandomParticle(Element element, int quantity);
}