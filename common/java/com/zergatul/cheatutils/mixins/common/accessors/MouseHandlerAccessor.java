package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

    @Invoker("onMove")
    void onMove_CU(long windowHandle, double x, double y);

    @Invoker("onButton")
    void onButton_CU(long windowHandle, MouseButtonInfo mouseButtonInfo, int i /*int button, int action, int mods*/);
}