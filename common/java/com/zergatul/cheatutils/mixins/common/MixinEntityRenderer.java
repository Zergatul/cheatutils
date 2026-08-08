package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    protected abstract boolean shouldShowName(Entity entity);

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"),
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private boolean onInvokeShouldShowName(EntityRenderer<?> instance, Entity entity) {
        if (!this.shouldShowName(entity)) {
            return false;
        }
        for (EntityEspConfig entityConfig : ConfigStore.instance.getConfig().entities.configs) {
            if (entityConfig.enabled && entityConfig.drawTitles && entityConfig.isValidEntity(entity)) {
                return false;
            }
        }
        return true;
    }
}