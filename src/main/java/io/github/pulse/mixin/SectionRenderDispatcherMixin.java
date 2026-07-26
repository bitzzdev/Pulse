package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.ChunkUploadOptimizer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderDispatcher.class)
public class SectionRenderDispatcherMixin {

    @Inject(method = "rebuildSectionSync", at = @At("HEAD"), require = 0)
    private void pulse$onRebuildSection(SectionRenderDispatcher.RenderSection section, RenderRegionCache cache, CallbackInfo ci) {
        ChunkUploadOptimizer opt = (ChunkUploadOptimizer) Pulse.getModuleManager().getModule(ModuleType.CHUNKS);
        if (opt != null && opt.isEnabled()) {
            opt.enqueueUpload(section);
        }
    }
}
