package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class TextureStateTracker {

    private static final Map<Integer, StateValue> minFilters = new HashMap<>();

    private TextureStateTracker() {
    }

    public static void setMinFilter(int textureId, int value) {
        StateValue state = minFilters.get(textureId);
        if (state == null) {
            int previous = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            state = new StateValue(previous);
            minFilters.put(textureId, state);
        }

        if (state.current != value) {
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, value);
            state.current = value;
        }
    }

    public static void restore() {
        for (Map.Entry<Integer, StateValue> entry : minFilters.entrySet()) {
            StateValue state = entry.getValue();
            if (state.current != state.previous) {
                GlStateManager._bindTexture(entry.getKey());
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, state.previous);
            }
        }

        minFilters.clear();
        GlStateManager._bindTexture(0);
    }

    private static class StateValue {

        private final int previous;
        private int current;

        private StateValue(int value) {
            previous = value;
            current = value;
        }
    }
}