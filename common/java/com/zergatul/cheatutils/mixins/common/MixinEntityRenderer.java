package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.extensions.EntityRenderStateExtension;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.modules.hacks.HitboxSize;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    protected abstract boolean shouldShowName(Entity entity, double distanceSquared);

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"),
            method = "extractRenderState")
    private boolean onInvokeShouldShowName(EntityRenderer<?, ?> renderer, Entity entity, double p_363875_) {
        if (!this.shouldShowName(entity, 1)) {
            return false;
        }
        for (EntityEspConfig entityConfig : ConfigStore.instance.getConfig().entities.configs) {
            if (entityConfig.enabled && entityConfig.drawTitles && entityConfig.isValidEntity(entity)) {
                return false;
            }
        }
        return true;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"),
            method = "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;")
    private AABB onGetEntityBoundingBox(Entity entity) {
        return HitboxSize.instance.get(entity);
    }

    @Inject(at = @At("TAIL"), method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;")
    private void onCreateRenderState(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> info) {
        EntityRenderStateExtension state = (EntityRenderStateExtension) info.getReturnValue();
        state.setParameters_CU(EntityEsp.instance.getEntityRenderParameters(entity));
    }
}