package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.font.*;
import com.zergatul.cheatutils.modules.esp.EspGlobal;
import com.zergatul.cheatutils.ui.*;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WorldMarkers implements FontBackendHolder {

    public static final WorldMarkers instance = new WorldMarkers();

    private final Minecraft mc = Minecraft.getInstance();

    private boolean fontChanged;
    private CompletableFuture<FontBackend> fontBackendFuture;
    private FontRenderer fontRenderer;

    private WorldMarkers() {
        Events.AfterRenderWorld.add(this::onRenderWorldLast, 10);

        WebApiRegistry.INSTANCE.register(new ConfigWebApi());
        WebApiRegistry.INSTANCE.register(new DimensionWebApi());
        WebApiRegistry.INSTANCE.register(new CoordinatesWebApi());

        FontBackendHolders.add(this);
    }

    @Override
    public boolean uses(FontBackend backend) {
        return fontRenderer != null && fontRenderer.uses(backend);
    }

    public void onFontChange() {
        ClientTickEndExecutor.instance.execute(() -> fontChanged = true);
    }

    private void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!EspGlobal.enabled) {
            return;
        }

        WorldMarkersConfig config = ConfigStore.instance.getConfig().worldMarkersConfig;
        if (!config.enabled) {
            return;
        }

        if (fontChanged) {
            fontBackendFuture = FontLibrary.instance.createBackend(config.font.asFontParameters());
            // fontRenderer = null; // more smooth transition, but shows prev font for few frames?
            fontChanged = false;
        }

        if (fontBackendFuture != null) {
            if (fontBackendFuture.isDone()) {
                fontRenderer = fontBackendFuture.join().createFontRenderer(config.font.asFontRenderDetails());
                fontBackendFuture = null;
            }
        }

        if (mc.level == null) {
            return;
        }

        if (fontRenderer == null) {
            return;
        }

        Vec3 view = event.getCameraPos();

        int scale = mc.getWindow().getGuiScale();
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

        try (RenderingContext context = new RenderingContext(matrix, scale)) {
            String dimension = mc.level.dimension().identifier().toString();
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

                Vector4f v1 = event.getViewRotation().transform(new Vector4f((float) x, (float) y, (float) z, 1));
                Vector4f v2 = event.getProjection().transform(v1);
                if (v2.w <= 0) {
                    continue; // behind
                }

                float invW = 1 / v2.w;
                int xc = Math.round(v2.x * invW * halfScrWidth);
                int yc = Math.round(-v2.y * invW * halfScrHeight);

                int color = entry.color.getRGB();
                int inverse = ColorUtils.inverse(color);

                StylizedText text = StylizedText.of(entry.name, entry.color.getRGB());
                FlexColumnElement flex = new FlexColumnElement();
                flex.append(
                        new DivisionElement()
                                .setBackgroundColor(inverse & 0x40FFFFFF)
                                .setBorderWidth(config.borderWidth)
                                .setBorderColor(entry.color.getRGB())
                                .setMargin(scale)
                                .setContent(
                                        new TextElement(fontRenderer, text)
                                                .setCompactHeight(true)));
                flex.append(
                        new RectangleElement(config.borderWidth, (int) fontRenderer.getLineHeight(), entry.color.getRGB()));

                context.render(flex, xc, yc - scale, HorizontalAlign.CENTER, VerticalAlign.BOTTOM);
            }
        }
    }

    private static final class ConfigWebApi extends SimpleModuleConfigWebApi<WorldMarkersConfig> {

        public ConfigWebApi() {
            super("world-markers", WorldMarkersConfig.class);
        }

        @Override
        protected WorldMarkersConfig getConfig() {
            return ConfigStore.instance.getConfig().worldMarkersConfig;
        }

        @Override
        protected void setConfig(WorldMarkersConfig config) {
            WorldMarkersConfig oldConfig = ConfigStore.instance.getConfig().worldMarkersConfig;
            ConfigStore.instance.getConfig().worldMarkersConfig = config;

            if (!oldConfig.font.equals(config.font)) {
                WorldMarkers.instance.onFontChange();
            }
        }
    }

    private static final class DimensionWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "dimension";
        }

        @Override
        public String get() throws ExecutionException, InterruptedException {
            return gson.toJson(ClientTickEndExecutor.instance.submit(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) {
                    return null;
                }
                return mc.level.dimension().identifier().toString();
            }).get());
        }
    }

    private static final class CoordinatesWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "coordinates";
        }

        @Override
        public String get() throws ExecutionException, InterruptedException {
            return gson.toJson(ClientTickEndExecutor.instance.submit(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) {
                    return null;
                }
                Vec3 pos = mc.player.getPosition(1.0f);
                return new Response(pos);
            }).get());
        }

        public static class Response {

            public double x;
            public double y;
            public double z;

            public Response(Vec3 pos) {
                this.x = pos.x;
                this.y = pos.y;
                this.z = pos.z;
            }
        }
    }
}