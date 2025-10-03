package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    protected EditBox input;

    @Shadow
    private int historyPos;

    @Inject(
            method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)V", shift = At.Shift.AFTER),
            cancellable = true)
    private void onAfterHandleChatInput(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> info) {
        if (ConfigStore.instance.getConfig().chatUtilitiesConfig.dontCloseChatOnEnter) {
            if (!this.input.getValue().isEmpty()) {
                this.input.setValue("");
                this.historyPos = Minecraft.getInstance().gui.getChat().getRecentChat().size();
                info.cancel();
            }
        }
    }
}