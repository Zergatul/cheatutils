package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

    @Invoker("onMove")
    void onMove_CU(long windowHandle, double x, double y);

    @Invoker("onPress")
    void onPress_CU(long windowHandle, int button, int action, int mods);
}