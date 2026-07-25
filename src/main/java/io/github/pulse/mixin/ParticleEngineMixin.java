package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.modules.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(CallbackInfoReturnable<Particle> cir) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage == OptimizationStage.NORMAL) return;

        ParticleOptimizer optimizer = (ParticleOptimizer) Pulse.getModuleManager().getModule(ModuleType.PARTICLES);
        if (optimizer == null || !optimizer.isEnabled()) return;

        if (!optimizer.shouldSpawnParticle("")) {
            cir.setReturnValue(null);
        }
    }
}
