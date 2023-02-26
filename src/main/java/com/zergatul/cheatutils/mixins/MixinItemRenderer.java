package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ItemRendererMixinInterface;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements ItemRendererMixinInterface {

    @Shadow
    @Final
    private TextureManager textureManager;

    @Override
    public TextureManager getTextureManager() {
        return this.textureManager;
    }
}