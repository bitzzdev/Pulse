package io.github.pulse.mixin;

import io.github.pulse.Pulse;
import io.github.pulse.api.ModuleType;
import io.github.pulse.api.OptimizationStage;
import io.github.pulse.api.PulseAPI;
import io.github.pulse.modules.CloudOptimizer;
import io.github.pulse.modules.WeatherOptimizer;
import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void onRenderClouds(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage == OptimizationStage.NORMAL) return;

        CloudOptimizer optimizer = (CloudOptimizer) Pulse.getModuleManager().getModule(ModuleType.CLOUDS);
        if (optimizer == null || !optimizer.isEnabled()) return;

        if (!optimizer.shouldRenderClouds()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(PoseStack poseStack, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        OptimizationStage stage = PulseAPI.getOptimizationStage();
        if (stage == OptimizationStage.NORMAL) return;

        WeatherOptimizer optimizer = (WeatherOptimizer) Pulse.getModuleManager().getModule(ModuleType.WEATHER);
        if (optimizer == null || !optimizer.isEnabled()) return;

        if (!optimizer.shouldRenderWeather()) {
            ci.cancel();
        }
    }
}
