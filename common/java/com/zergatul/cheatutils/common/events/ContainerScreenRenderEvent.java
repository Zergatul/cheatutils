package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public record ContainerScreenRenderEvent(
        AbstractContainerScreen<?> screen,
        GuiGraphics graphics,
        int leftPos,
        int topPos,
        int imageWidth) {}