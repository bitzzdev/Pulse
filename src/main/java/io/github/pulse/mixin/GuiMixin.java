package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.UIOptimizer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onRenderChat(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        UIOptimizer opt = (UIOptimizer) Pulse.getModuleManager().getModule(ModuleType.UI);
        if (opt != null && opt.isEnabled() && !opt.shouldUpdateChat()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onRenderTooltip(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        UIOptimizer opt = (UIOptimizer) Pulse.getModuleManager().getModule(ModuleType.UI);
        if (opt != null && opt.isEnabled() && opt.shouldSkipItemTooltipDecorations()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onRenderEffects(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        UIOptimizer opt = (UIOptimizer) Pulse.getModuleManager().getModule(ModuleType.UI);
        if (opt != null && opt.isEnabled() && opt.shouldSkipUiParticleEffects()) {
            ci.cancel();
        }
    }
}
