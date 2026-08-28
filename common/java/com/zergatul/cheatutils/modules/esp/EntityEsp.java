package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.EntityMaskRenderer;
import com.zergatul.cheatutils.render.InstancedCuboidLineRenderer;
import com.zergatul.cheatutils.render.InstancedTracerRenderer;
import com.zergatul.cheatutils.render.RenderTypeTextureResolver;
import com.zergatul.cheatutils.render.TextureStateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<EntityEspConfig, List<BufferedVerticesEntry>> overlayVertices = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<BufferedVerticesEntry>> outlineVertices = new IdentityHashMap<>();

    private EntityEsp() {
        Events.RenderWorldLast.add(this::render);
        Events.Close.add(this::close);
    }

    public MultiBufferSource modifyBufferSource(Entity entity, MultiBufferSource source) {
        if (mc.player == null || !EspGlobal.enabled) {
            return source;
        }

        EntityEspConfig overlayConfig = null;
        EntityEspConfig outlineConfig = null;
        double distanceSqr = entity.distanceToSqr(mc.player);
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (!config.enabled || !config.isValidEntity(entity)) {
                continue;
            }

            if (overlayConfig == null &&
                    config.drawOverlay &&
                    distanceSqr < config.getOverlayMaxDistanceSqr()) {
                overlayConfig = config;
            }
            if (outlineConfig == null &&
                    config.useModOutline() &&
                    distanceSqr < config.getOutlineMaxDistanceSqr()) {
                outlineConfig = config;
            }
            if (overlayConfig != null && outlineConfig != null) {
                break;
            }
        }

        if (overlayConfig == null && outlineConfig == null) {
            return source;
        }
        return new EntityMaskBufferSource(source, overlayConfig, outlineConfig);
    }

    private void render(RenderWorldLastEvent event) {
        if (!EspGlobal.enabled) {
            overlayVertices.clear();
            outlineVertices.clear();
            return;
        }

        float partialTicks = event.getTickDelta();

        Vec3 cameraPos = event.getCamera().getPosition();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        RenderUtilities utilities = RenderUtilities.instance;
        InstancedCuboidLineRenderer cuboidRenderer = utilities.getInstancedCuboidLineRenderer();
        InstancedTracerRenderer tracerRenderer = utilities.getInstancedTracerRenderer();
        cuboidRenderer.begin();
        tracerRenderer.begin();

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
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

            EntityEspConfig config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawBoundingBox &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getBoundingBoxMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                Vec3 pos = entity.getPosition(partialTicks);
                AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);
                cuboidRenderer.cuboid(
                        (float) (box.minX - cameraX),
                        (float) (box.minY - cameraY),
                        (float) (box.minZ - cameraZ),
                        (float) (box.maxX - cameraX),
                        (float) (box.maxY - cameraY),
                        (float) (box.maxZ - cameraZ),
                        config.boundingBoxColor,
                        (float) config.boundingBoxWidth);
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawTracers &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                Vec3 pos = entity.getPosition(partialTicks);
                tracerRenderer.tracer(
                        (float) (pos.x - cameraX),
                        (float) (pos.y - cameraY),
                        (float) (pos.z - cameraZ),
                        config.tracerColor,
                        (float) config.tracerWidth);
            }
        }

        tracerRenderer.end(event);
        cuboidRenderer.end(event);

        try {
            drawCapturedMasks(list, overlayVertices, event, false);
            drawCapturedMasks(list, outlineVertices, event, true);
        } finally {
            TextureStateTracker.restore();
            resetCapturedMasks(list);
        }
    }

    private void drawCapturedMasks(
            ImmutableList<EntityEspConfig> configs,
            Map<EntityEspConfig, List<BufferedVerticesEntry>> captured,
            RenderWorldLastEvent event,
            boolean outline
    ) {
        if (captured.isEmpty()) {
            return;
        }

        EntityMaskRenderer renderer = RenderUtilities.instance.getEntityMaskRenderer();
        for (EntityEspConfig config : configs) {
            List<BufferedVerticesEntry> entries = captured.get(config);
            if (entries == null || entries.isEmpty()) {
                continue;
            }

            renderer.begin(event);
            for (BufferedVerticesEntry entry : entries) {
                if (entry.vertices.size() == 0) {
                    continue;
                }
                AbstractTexture texture = mc.getTextureManager().getTexture(entry.texture);
                renderer.draw(entry.vertices, texture.getId());
            }
            if (outline) {
                renderer.endOutline(config.outlineColor);
            } else {
                renderer.endOverlay(config.overlayColor);
            }
        }
    }

    private FloatList getCapturedVertices(
            Map<EntityEspConfig, List<BufferedVerticesEntry>> captured,
            EntityEspConfig config,
            ResourceLocation texture
    ) {
        List<BufferedVerticesEntry> entries = captured.computeIfAbsent(config, key -> new ArrayList<>());
        for (BufferedVerticesEntry entry : entries) {
            if (entry.texture.equals(texture)) {
                return entry.vertices;
            }
        }

        BufferedVerticesEntry entry = new BufferedVerticesEntry(texture, new FloatList());
        entries.add(entry);
        return entry.vertices;
    }

    private void resetCapturedMasks(ImmutableList<EntityEspConfig> configs) {
        resetCapturedMasks(configs, overlayVertices);
        resetCapturedMasks(configs, outlineVertices);
    }

    private static void resetCapturedMasks(
            ImmutableList<EntityEspConfig> configs,
            Map<EntityEspConfig, List<BufferedVerticesEntry>> captured
    ) {
        captured.entrySet().removeIf(entry -> configs.stream().noneMatch(config -> config == entry.getKey()));
        for (List<BufferedVerticesEntry> entries : captured.values()) {
            for (BufferedVerticesEntry entry : entries) {
                entry.vertices.clear();
            }
        }
    }

    private void close() {
        RenderUtilities utilities = RenderUtilities.instance;
        utilities.getInstancedCuboidLineRenderer().close();
        utilities.getEntityMaskRenderer().close();
        overlayVertices.clear();
        outlineVertices.clear();
    }

    private static boolean isSupported(RenderType renderType) {
        if (renderType.outline().isEmpty() || renderType.mode() != VertexFormat.Mode.QUADS) {
            return false;
        }

        boolean position = false;
        boolean uv = false;
        for (VertexFormatElement element : renderType.format().getElements()) {
            if (element.getUsage() == VertexFormatElement.Usage.POSITION) {
                position = true;
            }
            if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 0) {
                uv = true;
            }
        }
        return position && uv;
    }

    private record BufferedVerticesEntry(ResourceLocation texture, FloatList vertices) {}

    private static class EntityMaskBufferSource implements MultiBufferSource {

        private final MultiBufferSource source;
        private final EntityEspConfig overlayConfig;
        private final EntityEspConfig outlineConfig;

        private EntityMaskBufferSource(
                MultiBufferSource source,
                EntityEspConfig overlayConfig,
                EntityEspConfig outlineConfig
        ) {
            this.source = source;
            this.overlayConfig = overlayConfig;
            this.outlineConfig = outlineConfig;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            VertexConsumer consumer = source.getBuffer(renderType);
            if (!isSupported(renderType) || consumer instanceof EntityMaskVertexConsumer) {
                return consumer;
            }

            ResourceLocation texture = RenderTypeTextureResolver.getTexture(renderType);
            if (texture == null) {
                return consumer;
            }

            FloatList overlay = overlayConfig == null ? null :
                    EntityEsp.instance.getCapturedVertices(EntityEsp.instance.overlayVertices, overlayConfig, texture);
            FloatList outline = outlineConfig == null ? null :
                    EntityEsp.instance.getCapturedVertices(EntityEsp.instance.outlineVertices, outlineConfig, texture);
            return new EntityMaskVertexConsumer(consumer, overlay, outline);
        }
    }

    private static class EntityMaskVertexConsumer implements VertexConsumer {

        private static final int[] TRIANGLE_INDICES = { 0, 1, 3, 1, 2, 3 };

        private final VertexConsumer consumer;
        private final FloatList overlay;
        private final FloatList outline;
        private final float[] quad = new float[20];
        private int quadVertices;
        private float x;
        private float y;
        private float z;
        private float u;
        private float v;
        private boolean hasPosition;
        private boolean hasUv;

        private EntityMaskVertexConsumer(VertexConsumer consumer, FloatList overlay, FloatList outline) {
            this.consumer = consumer;
            this.overlay = overlay;
            this.outline = outline;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            hasPosition = true;
            consumer.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            consumer.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            hasUv = true;
            consumer.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            consumer.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            consumer.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            consumer.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            consumer.endVertex();
            if (hasPosition && hasUv) {
                int index = quadVertices * 5;
                quad[index] = x;
                quad[index + 1] = y;
                quad[index + 2] = z;
                quad[index + 3] = u;
                quad[index + 4] = v;
                quadVertices++;
                if (quadVertices == 4) {
                    appendQuad(overlay);
                    appendQuad(outline);
                    quadVertices = 0;
                }
            } else {
                quadVertices = 0;
            }
            hasPosition = false;
            hasUv = false;
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            consumer.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            consumer.unsetDefaultColor();
        }

        private void appendQuad(FloatList target) {
            if (target == null) {
                return;
            }
            for (int vertex : TRIANGLE_INDICES) {
                int index = vertex * 5;
                target.add(quad[index]);
                target.add(quad[index + 1]);
                target.add(quad[index + 2]);
                target.add(quad[index + 3]);
                target.add(quad[index + 4]);
            }
        }
    }
}