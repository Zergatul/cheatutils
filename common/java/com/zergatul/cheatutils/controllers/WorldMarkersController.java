package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.render.ColorRender;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
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
        RenderSystem.recordRenderCall(() -> {
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
        double scale = mc.getWindow().getGuiScale();
        double invScale = 1 / scale;
        double scaledHalfWidth = mc.getWindow().getWidth() * invScale / 2;
        double scaledHalfHeight = mc.getWindow().getHeight() * invScale / 2;

        PoseStack poseStack = event.getGuiGraphics().pose();
        poseStack.pushPose();
        poseStack.last().pose().translate((float)scaledHalfWidth, (float)scaledHalfHeight, 0);

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

            double xc = v2.x / v2.w * scaledHalfWidth;
            double yc = -v2.y / v2.w * scaledHalfHeight;

            TextBounds bounds = fontRenderer.getTextSize(entry.name);
            double width = bounds.width() * invScale;
            double height = bounds.height() * invScale;

            double xp = xc - width / 2;
            yc -= 2 * height;
            double yp = yc;

            int color = entry.color.getRGB();
            int inverse = ColorUtils.inverse(color);

            double horizontalPadding = scale;
            double verticalPadding = scale;
            double rx1 = xp - horizontalPadding * invScale;
            double rx2 = xp + width + horizontalPadding * invScale;
            double ry1 = yp + (bounds.top() - verticalPadding) * invScale;
            double ry2 = yp + height - (bounds.bottom() - verticalPadding) * invScale;
            Primitives.fill(poseStack, rx1, ry1, rx2, ry2, inverse & 0x40FFFFFF);

            ColorRender.setShaderColor(color);
            fontRenderer.drawText(poseStack, entry.name, (float)xp, (float)yp, invScale);

            // border
            double bw = config.borderWidth * invScale;
            Primitives.fill(poseStack, rx1 - bw, ry1 - bw, rx2 + bw, ry1, color);
            Primitives.fill(poseStack, rx1 - bw, ry2, rx2 + bw, ry2 + bw, color);
            Primitives.fill(poseStack, rx1 - bw, ry1 - bw, rx1, ry2 + bw, color);
            Primitives.fill(poseStack, rx2, ry1 - bw, rx2 + bw, ry2 + bw, color);
            Primitives.fill(poseStack, xc - bw / 2, ry2, xc + bw / 2, ry2 + height, color);
        }

        poseStack.popPose();
    }
}