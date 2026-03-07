package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public record ContainerScreenRenderEvent(
        AbstractContainerScreen<?> screen,
        GuiGraphicsExtractor graphics,
        int leftPos,
        int topPos,
        int imageWidth) {}