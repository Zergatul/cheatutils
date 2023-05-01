package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Invoker("startUseItem")
    void startUseItem_CU();
}