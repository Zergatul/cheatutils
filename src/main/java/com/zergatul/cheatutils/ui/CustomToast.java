package com.zergatul.cheatutils.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@NullMarked
public class CustomToast implements IToast {

    private static final int DEFAULT_WIDTH = 160;
    private static final int HEIGHT = 32;
    private static final int BORDER = 4;
    private static final int TEXT_X = 18;
    private static final int RIGHT_MARGIN = 12;

    private final long durationMillis;
    private final String title;
    private final @Nullable String message;
    private final int width;

    public CustomToast(Duration duration, ITextComponent title, @Nullable ITextComponent message) {
        this.durationMillis = duration.toMillis();
        this.title = title.getFormattedText();
        this.message = message == null ? null : message.getFormattedText();

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        this.width = Math.max(DEFAULT_WIDTH, TEXT_X + RIGHT_MARGIN + Math.max(
                font.getStringWidth(this.title),
                this.message == null ? 0 : font.getStringWidth(this.message)));
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        Minecraft mc = toastGui.getMinecraft();
        mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);

        // Preserve the left section, including its icon, and tile only the plain background.
        toastGui.drawTexturedModalRect(0, 0, 0, 64, TEXT_X, HEIGHT);
        for (int x = TEXT_X; x < this.width - BORDER;) {
            int length = Math.min(DEFAULT_WIDTH - TEXT_X - BORDER, this.width - BORDER - x);
            toastGui.drawTexturedModalRect(x, 0, TEXT_X, 64, length, HEIGHT);
            x += length;
        }
        toastGui.drawTexturedModalRect(this.width - BORDER, 0, DEFAULT_WIDTH - BORDER, 64, BORDER, HEIGHT);

        if (this.message == null) {
            mc.fontRenderer.drawString(this.title, TEXT_X, 12, -256);
        } else {
            mc.fontRenderer.drawString(this.title, TEXT_X, 7, -256);
            mc.fontRenderer.drawString(this.message, TEXT_X, 18, -1);
        }

        return delta < this.durationMillis ? Visibility.SHOW : Visibility.HIDE;
    }
}
