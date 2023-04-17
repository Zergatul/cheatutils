package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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

    @Inject(at = @At("TAIL"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
    private void onRender(PoseStack p_95920_, int p_95921_, int p_95922_, float p_95923_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().deathCoordinatesConfig.enabled) {
            var screen = (Screen) (Object) this;
            var screenMixin = (ScreenAccessor) this;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                String text = "XYZ: " + format.format(mc.player.getX()) + " / " + format.format(mc.player.getY()) + " / " + format.format(mc.player.getZ());
                GuiComponent.drawCenteredString(p_95920_, screenMixin.getFont_CU(), text, screen.width / 2, 115, 16777215);
            }
        }
    }
}