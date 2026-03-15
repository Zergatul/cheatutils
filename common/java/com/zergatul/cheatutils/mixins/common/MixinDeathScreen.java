package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen extends Screen {

    private MixinDeathScreen(Component title) {
        super(title);
        throw new AssertionError();
    }

    @Inject(at = @At("TAIL"), method = "extractRenderState")
    private void onExtractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().deathCoordinatesConfig.enabled) {
            LocalPlayer player = this.minecraft.player;
            if (player != null) {
                DecimalFormat format = new DecimalFormat("0.000");
                String text = "XYZ: " + format.format(player.getX()) + " / " + format.format(player.getY()) + " / " + format.format(player.getZ());
                graphics.centeredText(this.font, text, this.width / 2, 115, 16777215);
            }
        }
    }
}