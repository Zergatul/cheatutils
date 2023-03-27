package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.ScreenMixinInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen {

    private final DecimalFormat format = new DecimalFormat("0.000");

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().deathCoordinatesConfig.enabled) {
            var screen = (Screen) (Object) this;
            var screenMixin = (ScreenMixinInterface) this;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                String text = "XYZ: " + format.format(mc.player.getX()) + " / " + format.format(mc.player.getY()) + " / " + format.format(mc.player.getZ());
                DrawableHelper.drawCenteredTextWithShadow(matrices, screenMixin.getFont(), text, screen.width / 2, 115, 16777215);
            }
        }
    }
}