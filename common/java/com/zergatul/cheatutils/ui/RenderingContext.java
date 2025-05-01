package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.Color2dRenderer;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RenderingContext {

    private final GuiGraphics graphics;
    private final Matrix4f matrix;
    private final int halfWidth;
    private final int halfHeight;
    private final int scale;
    private final Font font;

    private Color2dRenderer color2dRenderer;
    private Int2ObjectMap<FloatList> textureColor2dBuffers;
    private List<ItemStackRenderEntry> itemStacksQueue;

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight) {
        this.graphics = graphics;
        this.matrix = matrix;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;

        Minecraft mc = Minecraft.getInstance();
        this.scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        this.font = mc.font;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public Matrix4f getMatrix() {
        return matrix;
    }

    public int getScale() {
        return scale;
    }

    public Color2dRenderer getColor2dRenderer() {
        if (color2dRenderer == null) {
            color2dRenderer = RenderUtilities.instance.getColor2dRenderer();
            color2dRenderer.begin();
        }

        return color2dRenderer;
    }

    public FloatList getTextureColor2dBuffer(int textureId) {
        if (textureColor2dBuffers == null) {
            // use simple array map, since we shouldn't have a lot of entries here
            textureColor2dBuffers = new Int2ObjectArrayMap<>();
        }
        FloatList list = textureColor2dBuffers.get(textureId);
        if (list == null) {
            list = new FloatArrayList(32);
            textureColor2dBuffers.put(textureId, list);
        }

        return list;
    }

    public void queueItemStackRender(LivingEntity entity, ItemStack itemStack, int x, int y) {
        if (itemStacksQueue == null) {
            itemStacksQueue = new ArrayList<>(6);
        }
        itemStacksQueue.add(new ItemStackRenderEntry(entity, itemStack, x, y));
    }

    public void render(Element element, int x, int y, HorizontalAlign hAlign, VerticalAlign vAlign) {
        element.measure(this);

        int width = element.getMeasuredWidth();
        int height = element.getMeasuredHeight();

        if (hAlign == HorizontalAlign.CENTER) {
            x -= width / 2;
        } else {
            throw new RuntimeException();
        }
        if (vAlign == VerticalAlign.BOTTOM) {
            y -= height;
        } else {
            throw new RuntimeException();
        }

        element.layout(x, y, width, height);
        reset();
        element.render(this);

        if (color2dRenderer != null) {
            MainFrameBuffer.enter();
            color2dRenderer.end(matrix);
        }

        if (textureColor2dBuffers != null && !textureColor2dBuffers.isEmpty()) {
            MainFrameBuffer.enter();
            TextureColor2dRenderer renderer =  RenderUtilities.instance.getTextureColor2dRenderer();
            for (int textureId : textureColor2dBuffers.keySet()) {
                renderer.begin();
                renderer.fill(textureColor2dBuffers.get(textureId));
                renderer.end(matrix, textureId);
            }
        }

        if (itemStacksQueue != null) {
            GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
            graphics.pose().pushPose();
            for (ItemStackRenderEntry entry : itemStacksQueue) {
                graphics.pose().setIdentity();
                graphics.pose().translate(1d * (entry.x + halfWidth) / scale, 1d * (entry.y + halfHeight) / scale, 0);
                graphics.renderItem(entry.entity, entry.itemStack, 0, 0, 0);
                graphics.renderItemDecorations(font, entry.itemStack, 0, 0);
            }
            graphics.pose().popPose();
            GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
        }
    }

    private void reset() {
        color2dRenderer = null;
        if (textureColor2dBuffers != null) {
            textureColor2dBuffers.clear();
        }
        if (itemStacksQueue != null) {
            itemStacksQueue.clear();
        }
    }

    private record ItemStackRenderEntry(LivingEntity entity, ItemStack itemStack, int x, int y) {}
}