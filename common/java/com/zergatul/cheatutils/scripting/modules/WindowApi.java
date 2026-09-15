package com.zergatul.cheatutils.scripting.modules;

import com.mojang.blaze3d.platform.Window;
import com.zergatul.cheatutils.mixins.common.accessors.KeyboardHandlerAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.MouseHandlerAccessor;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
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
        handler.onMove_CU(window.handle(), x, y, x - handler.getXPos_CU(), y - handler.getYPos_CU());
    }

    @MethodDescription("""
            Low level method to make Minecraft think you changed state of mouse button.
            button=0 for left, =1 for right, =2 for middle.
            """)
    public void emulateMouseButtonEvent(int button, boolean pressed) {
        MouseHandlerAccessor handler = (MouseHandlerAccessor) mc.mouseHandler;
        handler.onButton_CU(window.handle(), new MouseButtonInfo(button, 0), pressed ? 1 : 0);
    }

    @MethodDescription("""
            Low level method to make Minecraft think you changed state of mouse button.
            Use Java<com.mojang.blaze3d.platform.InputConstants> to get constants from static fields:
            button - one of MOUSE_BUTTON_* fields
            modifiers - bit mask of MOD_* fields
            action - one of RELEASE/PRESS fields
            """)
    public void emulateMouseButtonEvent(int button, int modifiers, int action) {
        MouseHandlerAccessor handler = (MouseHandlerAccessor) mc.mouseHandler;
        handler.onButton_CU(window.handle(), new MouseButtonInfo(button, modifiers), action);
    }

    @MethodDescription("""
            Low level method to make Minecraft think you changed state of keyboard button.
            Use Java<com.mojang.blaze3d.platform.InputConstants> to get constants from static fields:
            key - one of KEY_* fields
            scancode - only used when key is -1
            modifiers - bit mask of MOD_* fields
            action - one of RELEASE/PRESS/REPEAT fields
            """)
    public void emulateKeyPressEvent(int key, int scancode, int modifiers, int action) {
        KeyboardHandlerAccessor handler = (KeyboardHandlerAccessor) mc.keyboardHandler;
        handler.keyPress_CU(window.handle(), action, new KeyEvent(key, scancode, modifiers));
    }

    @MethodDescription("""
            Low level method to make Minecraft think you typed character.
            """)
    public void emulateCharTypedEvent(int codepoint) {
        KeyboardHandlerAccessor handler = (KeyboardHandlerAccessor) mc.keyboardHandler;
        handler.charTyped_CU(window.handle(), new CharacterEvent(codepoint));
    }
}