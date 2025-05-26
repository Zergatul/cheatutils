package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.font.*;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import com.zergatul.cheatutils.ui.*;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.concurrent.CompletableFuture;

public class WorldMarkers implements GlyphRendererHolder {

    public static final WorldMarkers instance = new WorldMarkers();

    private final Minecraft mc = Minecraft.getInstance();
    private CompletableFuture<GlyphRenderer> glyphRendererFuture;
    private FontRenderer fontRenderer;

    private WorldMarkers() {
        Events.PreRenderGui.add(this::onPreRenderGui);
    }

    @Override
    public boolean uses(GlyphRenderer renderer) {
        return fontRenderer != null && fontRenderer.uses(renderer);
    }

    public void onFontChange(WorldMarkersConfig config) {
        TickEndExecutor.instance.execute(() -> {
            glyphRendererFuture = FontLibrary2.instance.createRenderer(config.font.asFontParameters());
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

        if (glyphRendererFuture != null) {
            if (glyphRendererFuture.isDone()) {
                fontRenderer = glyphRendererFuture.join().createFontRenderer(config.font.asFontRenderDetails());
                glyphRendererFuture = null;
            }
        }

        if (mc.level == null) {
            return;
        }

        if (fontRenderer == null) {
            return;
        }

        /*GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
        MainFrameBuffer.enter();*/

        Camera camera = event.getCamera();
        Vec3 view = camera.getPosition();

        int scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

        RenderingContext context = new RenderingContext(event.graphics(), matrix, halfScrWidth, halfScrHeight);

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

            int color = entry.color.getRGB();
            int inverse = ColorUtils.inverse(color);

            StylizedText text = StylizedText.of(entry.name, Style.EMPTY.withColor(entry.color.getRGB()));
            FlexColumnElement flex = new FlexColumnElement();
            flex.append(
                    new DivisionElement()
                            .setBackgroundColor(inverse & 0x40FFFFFF)
                            .setBorderWidth(config.borderWidth)
                            .setBorderColor(entry.color.getRGB())
                            .setMargin(context.getScale())
                            .setContent(
                                    new TextElement(fontRenderer, text)
                                            .setCompactHeight(true)));
            flex.append(
                    new RectangleElement(config.borderWidth, (int) fontRenderer.getLineHeight(), entry.color.getRGB()));

            context.render(flex, xc, yc - scale, HorizontalAlign.CENTER, VerticalAlign.BOTTOM);
        }

        /*MainFrameBuffer.exit();
        GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);*/
    }
}