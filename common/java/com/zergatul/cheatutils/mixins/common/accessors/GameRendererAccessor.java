package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Invoker("pick")
    HitResult pick_CU(Entity entity, double blockRange, double entityRange, float partialTicks);
}