package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.esp.EspGlobal;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(
            method = "extractNameplates(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractNamePlates(
            Entity entity,
            EntityRenderState state,
            float partialTicks,
            double nameplateDistance,
            double belowNameDistance,
            CallbackInfo info
    ) {
        if (EspGlobal.enabled) {
            for (EntityEspConfig entityConfig : ConfigStore.instance.getConfig().entities.configs) {
                if (entityConfig.enabled && entityConfig.drawTitles && entityConfig.isValidEntity(entity)) {
                    info.cancel();
                    return;
                }
            }
        }
    }
}