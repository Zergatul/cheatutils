package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.common.ModLifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor;<init>(Ljava/lang/Thread;)V", unsafe = true))
    private void onClientCreated(GameConfig gameConfig, CallbackInfo info) {
        ModLifecycle.onClientCreated();
    }
}