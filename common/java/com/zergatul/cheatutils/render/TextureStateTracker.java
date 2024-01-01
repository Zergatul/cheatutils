package com.zergatul.cheatutils.render;

import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.Map;

public class TextureStateTracker {

    private static final Map<Integer, StateValue> minFilters = new HashMap<>();

    public static void setTextureMinFilter(int textureId, int value) {
        StateValue state = minFilters.get(textureId);
        if (state == null) {
            int previous = GL30.glGetTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER);
            state = new StateValue();
            state.previous = previous;
            state.current = previous;
            minFilters.put(textureId, state);
            if (value != previous) {
                GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, value);
                state.current = value;
            }
        } else {
            if (state.current != value) {
                GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, value);
                state.current = value;
            }
        }
    }

    public static void restore() {
        for (Map.Entry<Integer, StateValue> entry : minFilters.entrySet()) {
            int textureId = entry.getKey();
            StateValue state = entry.getValue();
            if (state.previous != state.current) {
                GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
                GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, state.previous);
            }
        }

        minFilters.clear();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }

    private static class StateValue {
        public int previous;
        public int current;
    }
}