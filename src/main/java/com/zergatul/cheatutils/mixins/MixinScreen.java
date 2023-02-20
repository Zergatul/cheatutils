package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ScreenMixinInterface;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class MixinScreen implements ScreenMixinInterface {

    @Shadow
    protected abstract void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y);

    @Shadow
    protected TextRenderer textRenderer;

    @Override
    public TextRenderer getFont() {
        return textRenderer;
    }

    @Override
    public void renderTooltip2(MatrixStack matrices, ItemStack itemStack, int mouseX, int mouseY) {
        renderTooltip(matrices, itemStack, mouseX, mouseY);
    }
}