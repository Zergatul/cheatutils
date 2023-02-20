package com.zergatul.cheatutils.interfaces;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public interface ScreenMixinInterface {
    TextRenderer getFont();
    void renderTooltip2(MatrixStack poseStack, ItemStack itemStack, int mouseX, int mouseY);
}