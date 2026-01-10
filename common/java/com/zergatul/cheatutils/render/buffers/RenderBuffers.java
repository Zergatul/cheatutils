package com.zergatul.cheatutils.render.buffers;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.Color2dRenderer;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.joml.Matrix4f;

public class RenderBuffers {

    private Color2dRenderBuffer color2d;
    private Int2ObjectMap<TextureColor2dRenderBuffer> texColor2d;

    public Color2dRenderBuffer getColor2d() {
        if (color2d == null) {
            color2d = new Color2dRenderBuffer();
        }
        return color2d;
    }

    public TextureColor2dRenderBuffer getTexColor2d(int textureId) {
        if (texColor2d == null) {
            // use simple array map, since we shouldn't have a lot of entries here
            texColor2d = new Int2ObjectArrayMap<>();
        }
        if (!texColor2d.containsKey(textureId)) {
            texColor2d.put(textureId, new TextureColor2dRenderBuffer());
        }
        return texColor2d.get(textureId);
    }

    public void render(Matrix4f matrix, Runnable framebufferSetup) {
        if (isEmpty()) {
            return;
        }

        framebufferSetup.run();

        if (color2d != null) {
            Color2dRenderer renderer = RenderUtilities.instance.getColor2dRenderer();
            renderer.begin();
            renderer.fill(color2d.getList());
            renderer.end(matrix);
            color2d.clear();
        }

        if (hasTexColor2dData()) {
            TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
            IntIterator iterator = texColor2d.keySet().iterator();
            while (iterator.hasNext()) {
                int textureId = iterator.nextInt();
                renderer.begin();
                TextureColor2dRenderBuffer buffer = texColor2d.get(textureId);
                renderer.fill(buffer.getList());
                renderer.end(matrix, textureId, true);
                buffer.clear();
            }
        }
    }

    private boolean isEmpty() {
        if (color2d != null && !color2d.getList().isEmpty()) {
            return false;
        }
        if (hasTexColor2dData()) {
            return false;
        }
        return true;
    }

    private boolean hasTexColor2dData() {
        return texColor2d != null && !texColor2d.values().stream().allMatch(b -> b.getList().isEmpty());
    }
}