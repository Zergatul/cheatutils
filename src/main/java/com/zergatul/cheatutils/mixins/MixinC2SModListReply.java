package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.ModMain;
import net.minecraftforge.network.HandshakeMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandshakeMessages.C2SModListReply.class)
public class MixinC2SModListReply {

    @Shadow(remap = false)
    private List<String> mods;

    @Inject(at = @At("TAIL"), method = "Lnet/minecraftforge/network/HandshakeMessages$C2SModListReply;<init>()V", remap = false)
    public void onConstructor(CallbackInfo info) {
        mods.remove(ModMain.MODID);
    }
}