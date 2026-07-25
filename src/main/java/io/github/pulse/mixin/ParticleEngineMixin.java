package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onAddParticle(Particle particle, CallbackInfo ci) {
        ParticleOptimizer opt = (ParticleOptimizer) Pulse.getModuleManager().getModule(ModuleType.PARTICLES);
        if (opt != null && opt.isEnabled() && !opt.shouldRenderParticle(particle)) {
            ci.cancel();
        }
    }
}
