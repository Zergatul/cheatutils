package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.render.Primitives;
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

        Camera camera = event.getCamera();
        Vec3 view = camera.getPosition();

        float scale = (float) mc.getWindow().getGuiScale();
        float invScale = 1 / scale;
        float scaledHalfWidth = mc.getWindow().getWidth() * invScale / 2;
        float scaledHalfHeight = mc.getWindow().getHeight() * invScale / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-scaledHalfWidth, scaledHalfWidth, scaledHalfHeight, -scaledHalfHeight, -1, 1);

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

            float xc = v2.x / v2.w * scaledHalfWidth;
            float yc = -v2.y / v2.w * scaledHalfHeight;

            TextBounds bounds = fontRenderer.getTextSize(entry.name);
            float width = bounds.width() * invScale;
            float height = bounds.height() * invScale;

            float xp = xc - width / 2;
            yc -= 2 * height;
            float yp = yc;

            int color = entry.color.getRGB();
            int inverse = ColorUtils.inverse(color);

            float horizontalPadding = scale;
            float verticalPadding = scale;
            float rx1 = xp - horizontalPadding * invScale;
            float rx2 = xp + width + horizontalPadding * invScale;
            float ry1 = yp + (bounds.top() - verticalPadding) * invScale;
            float ry2 = yp + height - (bounds.bottom() - verticalPadding) * invScale;
            float width2 = rx2 - rx1;
            float height2 = ry2 - ry1;
            Primitives.fill(matrix, rx1, ry1, width2, height2, inverse & 0x40FFFFFF);

            fontRenderer.drawText(matrix, entry.name, xp, yp, invScale, color);

            // border
            float bw = config.borderWidth * invScale;
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
    }
}