package com.zergatul.cheatutils.ui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

@NullMarked
public class CustomToast implements Toast {

    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/system");
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private static final int TEXT_X_START = 18;

    private final Duration duration;
    private List<FormattedCharSequence> titleLines;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private int width;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    public CustomToast(Duration duration, Component title, @Nullable Component message) {
        this.duration = duration;
        this.update(title, message);
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        int titleHeight = (this.titleLines.size() - 1) * LINE_SPACING;
        int messageHeight = Math.max(this.messageLines.size(), 1) * LINE_SPACING;
        return 2 * MARGIN + titleHeight + messageHeight;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        if (this.changed) {
            this.lastChanged = fullyVisibleForMs;
            this.changed = false;
        }

        double timeToDisplayUpdate = this.duration.toMillis() * manager.getNotificationDisplayTimeMultiplier();
        long timeSinceUpdate = fullyVisibleForMs - this.lastChanged;
        this.wantedVisibility = timeSinceUpdate < timeToDisplayUpdate ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        if (this.messageLines.isEmpty()) {
            this.extractTextLines(graphics, font, this.titleLines, LINE_SPACING, -256);
        } else {
            this.extractTextLines(graphics, font, this.titleLines, 7, -256);
            this.extractTextLines(graphics, font, this.messageLines, 7 + this.titleLines.size() * LINE_SPACING, -1);
        }
    }

    private void extractTextLines(
            GuiGraphicsExtractor graphics,
            Font font,
            List<FormattedCharSequence> textLines,
            int yStart,
            int textColor
    ) {
        for (int i = 0; i < textLines.size(); i++) {
            graphics.text(font, textLines.get(i), TEXT_X_START, yStart + i * LINE_SPACING, textColor, false);
        }
    }

    private void update(final Component title, final @Nullable Component message) {
        this.titleLines = splitToLength(title);
        this.messageLines = nullToEmpty(message);
        this.recalculateWidth();
    }

    private void recalculateWidth() {
        int width = Math.max(
                Toast.DEFAULT_WIDTH,
                Stream.concat(this.titleLines.stream(), this.messageLines.stream())
                        .mapToInt(Minecraft.getInstance().font::width)
                        .max()
                        .orElse(MAX_LINE_SIZE));
        this.width = width + 3 * MARGIN;
    }

    private static List<FormattedCharSequence> nullToEmpty(final @Nullable Component text) {
        return text == null ? ImmutableList.of() : splitToLength(text);
    }

    private static List<FormattedCharSequence> splitToLength(final Component text) {
        return Minecraft.getInstance().font.split(text, MAX_LINE_SIZE);
    }
}