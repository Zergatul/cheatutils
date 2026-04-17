package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class MixinLevelExtractor {

    @Inject(
            method = "extractVisibleEntities",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private void onExtract(
            Camera camera,
            Frustum frustum,
            DeltaTracker deltaTracker,
            LevelRenderState output,
            CallbackInfo info,
            @Local(name = "entity") Entity entity,
            @Local(name = "state") EntityRenderState state
    ) {
        EntityEsp.instance.captureEntityRenderState(entity, state);
    }
}