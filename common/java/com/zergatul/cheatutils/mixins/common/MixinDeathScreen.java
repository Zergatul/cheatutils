package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen {

    private DecimalFormat format = new DecimalFormat("0.000");

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    private void onRender(GuiGraphics graphics, int p_95921_, int p_95922_, float p_95923_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().deathCoordinatesConfig.enabled) {
            var screen = (Screen) (Object) this;
            var screenMixin = (ScreenAccessor) this;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                String text = "XYZ: " + format.format(mc.player.getX()) + " / " + format.format(mc.player.getY()) + " / " + format.format(mc.player.getZ());
                graphics.drawCenteredString(screenMixin.getFont_CU(), text, screen.width / 2, 115, 16777215);
            }
        }
    }
}