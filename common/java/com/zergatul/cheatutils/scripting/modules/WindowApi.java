package com.zergatul.cheatutils.scripting.modules;

import com.mojang.blaze3d.platform.Window;
import com.zergatul.cheatutils.mixins.common.accessors.MouseHandlerAccessor;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;

@SuppressWarnings("unused")
public class WindowApi {

    private final Minecraft mc = Minecraft.getInstance();
    private final Window window = mc.getWindow();

    @MethodDescription("Display pixels per Minecraft pixels")
    public int getGuiScale() {
        return (int) window.getGuiScale();
    }

    @MethodDescription("Width of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiWidth() {
        return window.getGuiScaledWidth();
    }

    @MethodDescription("Height of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiHeight() {
        return window.getGuiScaledHeight();
    }

    @MethodDescription("Width of client area of Minecraft window in pixels")
    public int getWidth() {
        return window.getWidth();
    }

    @MethodDescription("Height of client area of Minecraft window in pixels")
    public int getHeight() {
        return window.getHeight();
    }

    @MethodDescription("""
            Low level method to make Minecraft think you moved mouse cursor.
            Doesn't actually change the real mouse position.
            Coordinates are in pixels from top-left corner.
            """)
    public void emulateMouseMove(int x, int y) {
        MouseHandlerAccessor handler = (MouseHandlerAccessor) mc.mouseHandler;
        handler.onMove_CU(window.handle(), x, y);
    }

    @MethodDescription("""
            Low level method to make Minecraft think you changed state of mouse button.
            button=0 for left, =1 for right, =2 for middle.
            """)
    public void emulateMouseButtonEvent(int button, boolean pressed) {
        MouseHandlerAccessor handler = (MouseHandlerAccessor) mc.mouseHandler;
        handler.onButton_CU(window.handle(), new MouseButtonInfo(button, 0), pressed ? 1 : 0);
    }
}