package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.AnimationOptimizer;
import io.github.pulse.modules.EntityOptimizer;
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
    private <E extends Entity> void pulse$onShouldRender(E entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        EntityOptimizer eo = (EntityOptimizer) Pulse.getModuleManager().getModule(ModuleType.ENTITIES);
        if (eo != null && eo.isEnabled() && !eo.shouldRenderEntity(entity)) {
            cir.setReturnValue(false);
            return;
        }
        AnimationOptimizer ao = (AnimationOptimizer) Pulse.getModuleManager().getModule(ModuleType.ANIMATIONS);
        if (ao != null && ao.isEnabled() && !ao.shouldAnimateEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
