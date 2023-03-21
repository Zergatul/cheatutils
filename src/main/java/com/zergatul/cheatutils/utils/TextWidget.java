package com.zergatul.cheatutils.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.awt.*;

public class TextWidget extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry {

    private int width;
    private int height;
    private int x;
    private int y;
    private String text;

    public TextWidget(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    @Override
    public void render(PoseStack poseStack, int p_94670_, int p_94671_, float p_94672_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        drawCenteredString(poseStack, font, text, this.x + this.width / 2, this.y + (this.height - 8) / 2, Color.WHITE.getRGB());
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }

    @Override
    public void setFocused(boolean p_265728_) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}