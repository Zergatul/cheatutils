package com.zergatul.cheatutils.mixins.fabric;

import com.zergatul.cheatutils.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor;<init>(Ljava/lang/Thread;)V"))
    private void onLoading(GameConfig gameConfig, CallbackInfo info) {
        Modules.lateRegister();
    }
}