package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    protected EditBox input;

    @Shadow
    private int historyPos;

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1))
    private void onSendChatMessageCloseChat(Minecraft mc, Screen screen) {
        if (ConfigStore.instance.getConfig().chatUtilitiesConfig.dontCloseChatOnEnter) {
            if (this.input.getValue().isEmpty()) {
                mc.setScreen(screen);
            } else {
                this.input.setValue("");
                this.historyPos = Minecraft.getInstance().gui.getChat().getRecentChat().size();
            }
        } else {
            mc.setScreen(screen);
        }
    }
}