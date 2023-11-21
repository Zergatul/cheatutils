package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeRenderTypeAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeStateAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.TextureStateShardAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.EntityOutlineRenderer;
import com.zergatul.cheatutils.render.EntityOverlayRenderer;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.RawLinesRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<EntityTracerConfig, List<BufferedVerticesEntry>> overlayBufferedVertices = new HashMap<>();
    private final Map<EntityTracerConfig, List<BufferedVerticesEntry>> outlineBufferedVertices = new HashMap<>();

    private EntityEsp() {
        Events.RenderWorldLast.add(this::render);
    }

    public MultiBufferSource onRenderEntityModifyBufferSource(Entity entity, MultiBufferSource bufferSource) {
        if (mc.player != null && ConfigStore.instance.getConfig().esp) {
            for (EntityTracerConfig config : ConfigStore.instance.getConfig().entities.configs) {
                if (!config.enabled) {
                    continue;
                }

                if (!config.isValidEntity(entity)) {
                    continue;
                }

                boolean drawOverlay = config.drawOverlay && entity.distanceToSqr(mc.player) < config.getOverlayMaxDistanceSqr();
                boolean drawOutline = config.useModOutline() && entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr();
                if (drawOverlay || drawOutline) {
                    return new EntityEsp.MultiBufferSourceWrapper(config, bufferSource, drawOverlay, drawOutline);
                }
            }
        }

        return bufferSource;
    }

    public boolean shouldEntityGlow(Entity entity) {
        if (!ConfigStore.instance.getConfig().esp) {
            return false;
        }
        if (mc.player == null) {
            return false;
        }
        for (EntityTracerConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr()) {
                return true;
            }
        }
        return false;
    }

    public Integer getGlowColor(Entity entity) {
        if (!ConfigStore.instance.getConfig().esp) {
            return null;
        }
        for (EntityTracerConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity)) {
                return config.glowColor.getRGB();
            }
        }
        return null;
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        float partialTicks = event.getTickDelta();

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, false);

        ImmutableList<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
        for (Entity entity : mc.player.clientLevel.entitiesForRendering()) {
            if (entity instanceof LocalPlayer) {
                continue;
            }

            if (entity.isRemoved()) {
                continue;
            }

            double dx = entity.getX() - playerX;
            double dy = entity.getY() - playerY;
            double dz = entity.getZ() - playerZ;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            EntityTracerConfig config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawOutline &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                renderEntityBounding(renderer, partialTicks, entity, config);
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                drawTracer(
                        renderer,
                        tracerX, tracerY, tracerZ,
                        entity.getPosition(event.getTickDelta()),
                        config);
            }
        }

        renderer.end();

        /**/
        /*var rrr = new RawLinesRenderer();
        rrr.begin();
        for (EntityTracerConfig config: vertices.keySet()) {
            List<Float> list1 = vertices.get(config);
            for (int i = 0; i < list1.size(); i += 12) {
                float x1 = list1.get(i);
                float y1 = list1.get(i + 1);
                float z1 = list1.get(i + 2);
                float x2 = list1.get(i + 3);
                float y2 = list1.get(i + 4);
                float z2 = list1.get(i + 5);
                float x3 = list1.get(i + 6);
                float y3 = list1.get(i + 7);
                float z3 = list1.get(i + 8);
                float x4 = list1.get(i + 9);
                float y4 = list1.get(i + 10);
                float z4 = list1.get(i + 11);
                rrr.quad(
                        x1, y1, z1,
                        x2, y2, z2,
                        x3, y3, z3,
                        x4, y4, z4);
            }
        }
        rrr.end(event.getProjectionMatrix());
        vertices.clear();*/

        drawOverlays(event);
        drawOutlines(event);

        /*if (rawLines == null) {
            rawLines = new RawLinesRenderer();
        }

        if (drawWireframe) {
            for (EntityTracerConfig config: bufferedVertices.keySet()) {
                List<BufferedVerticesEntry> entries = bufferedVertices.get(config);
                for (BufferedVerticesEntry entry: entries) {
                    FloatList flist = entry.list;
                    if (flist.size() == 0) {
                        continue;
                    }

                    rawLines.begin();

                    int size = flist.size();
                    int i = 0;
                    while (i < size) {
                        float x1 = flist.get(i++);
                        float y1 = flist.get(i++);
                        float z1 = flist.get(i++);
                        float u1 = flist.get(i++);
                        float v1 = flist.get(i++);
                        float x2 = flist.get(i++);
                        float y2 = flist.get(i++);
                        float z2 = flist.get(i++);
                        float u2 = flist.get(i++);
                        float v2 = flist.get(i++);
                        float x3 = flist.get(i++);
                        float y3 = flist.get(i++);
                        float z3 = flist.get(i++);
                        float u3 = flist.get(i++);
                        float v3 = flist.get(i++);
                        float x4 = flist.get(i++);
                        float y4 = flist.get(i++);
                        float z4 = flist.get(i++);
                        float u4 = flist.get(i++);
                        float v4 = flist.get(i++);
                        rawLines.line(x1, y1, z1, x2, y2, z2);
                        rawLines.line(x2, y2, z2, x3, y3, z3);
                        rawLines.line(x3, y3, z3, x4, y4, z4);
                        rawLines.line(x4, y4, z4, x1, y1, z1);
                    }

                    rawLines.end(event.getProjectionMatrix());
                }
            }
        }*/
    }

    private RawLinesRenderer rawLines;
    private boolean drawWireframe;

    private static void renderEntityBounding(LineRenderer renderer, float partialTicks, Entity entity, EntityTracerConfig config) {
        Vec3 pos = entity.getPosition(partialTicks);
        AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);

        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;
        float a = config.outlineColor.getAlpha() / 255f;

        renderer.cuboid(
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, a);
    }

    private static void drawTracer(LineRenderer renderer, double tx, double ty, double tz, Vec3 pos, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;
        float a = config.tracerColor.getAlpha() / 255f;

        renderer.line(tx, ty, tz, pos.x, pos.y, pos.z, r, g, b, a);
    }

    private void drawOverlays(RenderWorldLastEvent event) {
        EntityOverlayRenderer renderer = RenderUtilities.instance.getEntityOverlayRenderer();
        for (EntityTracerConfig config: overlayBufferedVertices.keySet()) {
            renderer.begin();

            List<BufferedVerticesEntry> entries = overlayBufferedVertices.get(config);
            for (BufferedVerticesEntry entry: entries) {
                FloatList list = entry.list;
                if (list.size() == 0) {
                    continue;
                }

                int size = list.size();
                int i = 0;
                while (i < size) {
                    float x1 = list.get(i++);
                    float y1 = list.get(i++);
                    float z1 = list.get(i++);
                    float u1 = list.get(i++);
                    float v1 = list.get(i++);
                    float x2 = list.get(i++);
                    float y2 = list.get(i++);
                    float z2 = list.get(i++);
                    float u2 = list.get(i++);
                    float v2 = list.get(i++);
                    float x3 = list.get(i++);
                    float y3 = list.get(i++);
                    float z3 = list.get(i++);
                    float u3 = list.get(i++);
                    float v3 = list.get(i++);
                    float x4 = list.get(i++);
                    float y4 = list.get(i++);
                    float z4 = list.get(i++);
                    float u4 = list.get(i++);
                    float v4 = list.get(i++);
                    renderer.quad(
                            x1, y1, z1, u1, v1,
                            x2, y2, z2, u2, v2,
                            x3, y3, z3, u3, v3,
                            x4, y4, z4, u4, v4);
                }

                AbstractTexture texture = mc.getTextureManager().getTexture(entry.texture);
                renderer.renderBuffer(event.getProjectionMatrix(), texture.getId());
            }

            renderer.end(
                    config.overlayColor.getRed() / 255f,
                    config.overlayColor.getGreen() / 255f,
                    config.overlayColor.getBlue() / 255f,
                    config.overlayColor.getAlpha() / 255f);
        }

        overlayBufferedVertices.clear();
    }

    private void drawOutlines(RenderWorldLastEvent event) {
        EntityOutlineRenderer renderer = RenderUtilities.instance.getEntityOutlineRenderer();
        for (EntityTracerConfig config: outlineBufferedVertices.keySet()) {
            renderer.begin();

            List<BufferedVerticesEntry> entries = outlineBufferedVertices.get(config);
            for (BufferedVerticesEntry entry: entries) {
                FloatList list = entry.list;
                if (list.size() == 0) {
                    continue;
                }

                int size = list.size();
                int i = 0;
                while (i < size) {
                    float x1 = list.get(i++);
                    float y1 = list.get(i++);
                    float z1 = list.get(i++);
                    float u1 = list.get(i++);
                    float v1 = list.get(i++);
                    float x2 = list.get(i++);
                    float y2 = list.get(i++);
                    float z2 = list.get(i++);
                    float u2 = list.get(i++);
                    float v2 = list.get(i++);
                    float x3 = list.get(i++);
                    float y3 = list.get(i++);
                    float z3 = list.get(i++);
                    float u3 = list.get(i++);
                    float v3 = list.get(i++);
                    float x4 = list.get(i++);
                    float y4 = list.get(i++);
                    float z4 = list.get(i++);
                    float u4 = list.get(i++);
                    float v4 = list.get(i++);
                    renderer.quad(
                            x1, y1, z1, u1, v1,
                            x2, y2, z2, u2, v2,
                            x3, y3, z3, u3, v3,
                            x4, y4, z4, u4, v4);
                }

                AbstractTexture texture = mc.getTextureManager().getTexture(entry.texture);
                renderer.renderBuffer(event.getProjectionMatrix(), texture.getId());
            }

            renderer.end(
                    config.glowColor.getRed() / 255f,
                    config.glowColor.getGreen() / 255f,
                    config.glowColor.getBlue() / 255f,
                    config.glowColor.getAlpha() / 255f);
        }

        outlineBufferedVertices.clear();
    }

    public static class MultiBufferSourceWrapper implements MultiBufferSource {

        private final EntityTracerConfig config;
        private final MultiBufferSource source;
        private final boolean overlay;
        private final boolean outline;

        public MultiBufferSourceWrapper(EntityTracerConfig config, MultiBufferSource source, boolean overlay, boolean outline) {
            this.config = config;
            this.source = source;
            this.overlay = overlay;
            this.outline = outline;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            if (!renderType.outline().isEmpty() && renderType.mode() == VertexFormat.Mode.QUADS) {
                if (renderType instanceof CompositeRenderTypeAccessor accessor) {
                    RenderType.CompositeState state = accessor.getState_CU();
                    RenderStateShard.EmptyTextureStateShard shard = ((CompositeStateAccessor) (Object) state).getTextureState_CU();
                    if (shard instanceof RenderStateShard.TextureStateShard textureStateShard) {
                        Optional<ResourceLocation> texture = ((TextureStateShardAccessor) textureStateShard).getTexture_CU();
                        if (texture.isPresent()) {
                            return new VertexConsumerWrapper(config, texture.get(), source.getBuffer(renderType), overlay, outline);
                        }
                    }
                }
            }

            return source.getBuffer(renderType);
        }
    }

    private static class VertexConsumerWrapper implements VertexConsumer {

        private final VertexConsumer consumer;
        private FloatList overlayList;
        private FloatList outlineList;

        public VertexConsumerWrapper(
                EntityTracerConfig config,
                ResourceLocation texture,
                VertexConsumer consumer,
                boolean overlay,
                boolean outline
        ) {
            this.consumer = consumer;

            if (overlay) {
                List<BufferedVerticesEntry> entries = EntityEsp.instance.overlayBufferedVertices.computeIfAbsent(config, c -> new ArrayList<>());
                for (BufferedVerticesEntry entry : entries) {
                    if (entry.texture.equals(texture)) {
                        overlayList = entry.list;
                        break;
                    }
                }

                if (overlayList == null) {
                    BufferedVerticesEntry entry = new BufferedVerticesEntry(texture, new FloatList());
                    entries.add(entry);
                    overlayList = entry.list;
                }
            }

            if (outline) {
                List<BufferedVerticesEntry> entries = EntityEsp.instance.outlineBufferedVertices.computeIfAbsent(config, c -> new ArrayList<>());
                for (BufferedVerticesEntry entry : entries) {
                    if (entry.texture.equals(texture)) {
                        outlineList = entry.list;
                        break;
                    }
                }

                if (outlineList == null) {
                    BufferedVerticesEntry entry = new BufferedVerticesEntry(texture, new FloatList());
                    entries.add(entry);
                    outlineList = entry.list;
                }
            }
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            if (overlayList != null) {
                overlayList.add((float) x);
                overlayList.add((float) y);
                overlayList.add((float) z);
            }
            if (outlineList != null) {
                outlineList.add((float) x);
                outlineList.add((float) y);
                outlineList.add((float) z);
            }
            return consumer.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return consumer.color(r, g, b, a);
        }

        @Override
        public VertexConsumer uv(float x, float y) {
            if (overlayList != null) {
                overlayList.add(x);
                overlayList.add(y);
            }
            if (outlineList != null) {
                outlineList.add(x);
                outlineList.add(y);
            }
            return consumer.uv(x, y);
        }

        @Override
        public VertexConsumer overlayCoords(int x, int y) {
            return consumer.overlayCoords(x, y);
        }

        @Override
        public VertexConsumer uv2(int x, int y) {
            return consumer.uv2(x, y);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return consumer.normal(x, y, z);
        }

        @Override
        public void endVertex() {
            consumer.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            consumer.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            consumer.unsetDefaultColor();
        }
    }

    private record BufferedVerticesEntry(ResourceLocation texture, FloatList list) {}
}