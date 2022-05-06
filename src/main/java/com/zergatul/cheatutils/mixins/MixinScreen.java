package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.interfaces.ScreenMixinInterface;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class MixinScreen implements ScreenMixinInterface {

    @Shadow
    protected abstract void renderTooltip(PoseStack p_96566_, ItemStack p_96567_, int p_96568_, int p_96569_);

    @Override
    public void renderTooltip2(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
        renderTooltip(poseStack, itemStack, mouseX, mouseY);
    }
}
