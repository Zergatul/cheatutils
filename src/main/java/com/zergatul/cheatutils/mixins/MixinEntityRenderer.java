package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.interfaces.EntityRendererMixinInterface;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements EntityRendererMixinInterface {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    protected abstract boolean shouldShowName(Entity entity);

    @Override
    public EntityRenderDispatcher getDispatcher() {
        return entityRenderDispatcher;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"),
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private boolean onInvokeShouldShowName(EntityRenderer<?> instance, Entity entity) {
        if (!this.shouldShowName(entity)) {
            return false;
        }
        for (EntityTracerConfig entityConfig : ConfigStore.instance.getConfig().entities.configs) {
            if (entityConfig.enabled && entityConfig.drawTitles && entityConfig.isValidEntity(entity)) {
                return false;
            }
        }
        return true;
    }
}