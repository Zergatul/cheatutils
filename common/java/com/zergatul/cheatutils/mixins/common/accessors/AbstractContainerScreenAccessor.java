package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("imageWidth")
    int getWidth_CU();

    @Accessor("imageHeight")
    int getHeight_CU();
}