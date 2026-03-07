package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;

public record RenderGuiEvent(GuiGraphics graphics) {}