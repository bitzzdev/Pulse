package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.modules.EntityOptimizer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void onRenderEntity(E entity, double x, double y, double z, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage == OptimizationStage.NORMAL) return;

        EntityOptimizer optimizer = (EntityOptimizer) Pulse.getModuleManager().getModule(ModuleType.ENTITIES);
        if (optimizer == null || !optimizer.isEnabled()) return;

        if (!optimizer.shouldRenderEntity(entity)) {
            ci.cancel();
        }
    }
}
