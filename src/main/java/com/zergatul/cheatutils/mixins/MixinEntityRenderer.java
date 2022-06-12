package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.EntityRendererMixinInterface;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements EntityRendererMixinInterface {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Override
    public EntityRenderDispatcher getDispatcher() {
        return entityRenderDispatcher;
    }
}
