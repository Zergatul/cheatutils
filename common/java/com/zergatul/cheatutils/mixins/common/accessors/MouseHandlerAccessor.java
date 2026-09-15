package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

    @Invoker("onMove")
    void onMove_CU(long handle, double x, double y, double deltaX, double deltaY);

    @Accessor("xpos")
    double getXPos_CU();

    @Accessor("ypos")
    double getYPos_CU();

    @Invoker("onButton")
    void onButton_CU(long handle, MouseButtonInfo mouseButtonInfo, int action);
}