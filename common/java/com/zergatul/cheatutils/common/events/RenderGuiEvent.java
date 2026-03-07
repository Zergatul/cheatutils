package com.zergatul.cheatutils.common.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public record RenderGuiEvent(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {}