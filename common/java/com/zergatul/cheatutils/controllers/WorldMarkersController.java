package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.*;

public class WorldMarkersController {

    public static final WorldMarkersController instance = new WorldMarkersController();

    private final Minecraft mc = Minecraft.getInstance();
    private GlyphFontRenderer fontRenderer;

    private WorldMarkersController() {
        Events.PreRenderGui.add(this::onPreRenderGui);
    }

    public void onFontChange(WorldMarkersConfig config) {
        TickEndExecutor.instance.execute(() -> {
            if (fontRenderer != null) {
                fontRenderer.dispose();
            }
            fontRenderer = new GlyphFontRenderer(new Font("Consolas", Font.PLAIN, config.fontSize), config.antiAliasing);
        });
    }

    private void onPreRenderGui(RenderGuiEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        WorldMarkersConfig config = ConfigStore.instance.getConfig().worldMarkersConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
        MainFrameBuffer.enter();

        Camera camera = event.getCamera();
        Vec3 view = camera.getPosition();

        int scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

        String dimension = mc.level.dimension().location().toString();
        for (WorldMarkersConfig.Entry entry : config.entries) {
            if (!entry.enabled) {
                continue;
            }
            if (!dimension.equals(entry.dimension)) {
                continue;
            }

            double x = entry.x - view.x;
            double y = entry.y - view.y;
            double z = entry.z - view.z;
            if (x * x + y * y + z * z < entry.minDistance * entry.minDistance) {
                continue;
            }

            Vector4f v1 = event.getWorldPoseMatrix().transform(new Vector4f((float)x, (float)y, (float)z, 1));
            Vector4f v2 = event.getWorldProjectionMatrix().transform(v1);
            if (v2.z <= 0) {
                continue; // behind
            }

            int xc = Math.round(v2.x / v2.w * halfScrWidth);
            int yc = Math.round(-v2.y / v2.w * halfScrHeight);

            TextBounds bounds = fontRenderer.getTextSize(entry.name);
            int width = bounds.width();
            int height = bounds.height();

            int xp = xc - width / 2;
            yc -= 2 * height;
            int yp = yc;

            int color = entry.color.getRGB();
            int inverse = ColorUtils.inverse(color);

            int rx1 = xp - scale;
            int rx2 = xp + width + scale;
            int ry1 = yp + (bounds.top() - scale);
            int ry2 = yp + height - (bounds.bottom() - scale);
            int width2 = rx2 - rx1;
            int height2 = ry2 - ry1;
            Primitives.fill(matrix, rx1, ry1, width2, height2, inverse & 0x40FFFFFF);

            fontRenderer.drawText(matrix, entry.name, xp, yp, color);

            // border
            int bw = config.borderWidth;
            Primitives.fill(matrix,
                    rx1 - bw, ry1 - bw,
                    width2 + 2 * bw, bw,
                    color);
            Primitives.fill(matrix,
                    rx1 - bw, ry2,
                    width2 + 2 * bw, bw,
                    color);
            Primitives.fill(matrix,
                    rx1 - bw, ry1 - bw,
                    bw, height2 + 2 * bw,
                    color);
            Primitives.fill(matrix,
                    rx2, ry1 - bw,
                    bw, height2 + 2 * bw,
                    color);

            Primitives.fill(matrix,
                    xc - bw / 2, ry2,
                    bw, height2,
                    color);
        }

        MainFrameBuffer.exit();
        GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
    }
}