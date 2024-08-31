package com.zergatul.cheatutils.mixins.neoforge.accessors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Accessor(value = "tooltipStack", remap = false)
    ItemStack getTooltipStack_CU();
}