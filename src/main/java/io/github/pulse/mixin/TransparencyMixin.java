package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.TransparencyOptimizer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSection.class)
public class TransparencyMixin {

    @Inject(method = "resortTransparency", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onResortTransparency(CallbackInfo ci) {
        TransparencyOptimizer opt = (TransparencyOptimizer) Pulse.getModuleManager().getModule(ModuleType.TRANSPARENCY);
        if (opt != null && opt.isEnabled() && !opt.shouldResortTranslucency()) {
            ci.cancel();
        }
    }
}
