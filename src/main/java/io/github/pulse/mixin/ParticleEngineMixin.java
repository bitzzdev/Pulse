package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.modules.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    private List<Particle> particles;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage == OptimizationStage.NORMAL) return;

        ParticleOptimizer optimizer = (ParticleOptimizer) Pulse.getModuleManager().getModule(ModuleType.PARTICLES);
        if (optimizer == null || !optimizer.isEnabled()) return;

        int maxParticles = optimizer.getMaxParticles();
        if (maxParticles <= 0 || particles.size() <= maxParticles) return;

        int toRemove = particles.size() - maxParticles;
        particles.subList(particles.size() - toRemove, particles.size()).clear();
    }
}
