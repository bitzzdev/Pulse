package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.modules.UIOptimizer;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void pulse$onTick(CallbackInfo ci) {
        UIOptimizer opt = (UIOptimizer) Pulse.getModuleManager().getModule(ModuleType.UI);
        if (opt != null && opt.isEnabled()) {
            opt.onChatTick();
        }
    }
}
