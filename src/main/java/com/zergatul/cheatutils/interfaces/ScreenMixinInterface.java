package com.zergatul.cheatutils.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public interface ScreenMixinInterface {
    void renderTooltip2(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY);
}
