package pw.dotdash.bending.api.ability;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;

public interface AbilityService {

    /**
     * Gets the singleton instance of the {@link AbilityService}.
     *
     * @return The singleton service instance
     */
    static AbilityService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(AbilityService.class);
    }

    long getAbilityDelayMilli();

    SpongeExecutorService getSyncExecutor();
}