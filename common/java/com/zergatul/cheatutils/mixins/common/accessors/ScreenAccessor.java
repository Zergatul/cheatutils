package com.zergatul.cheatutils.mixins.common.accessors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Accessor("font")
    Font getFont_CU();

    @Invoker("renderTooltip")
    void renderTooltip_CU(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY);
}