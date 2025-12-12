package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {

    @Invoker("pick")
    static HitResult pick_CU(Entity entity, double blockRange, double entityRange, float partialTicks) {
        throw new AssertionError();
    }
}
