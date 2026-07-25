package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.AnimationOptimizer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        AnimationOptimizer opt = (AnimationOptimizer) Pulse.getModuleManager().getModule(ModuleType.ANIMATIONS);
        if (opt != null && opt.isEnabled() && !opt.shouldAnimateEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
