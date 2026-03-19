package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.OverlayDrawProgram;
import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;
import com.zergatul.cheatutils.utils.ColorUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

import static com.zergatul.cheatutils.render.GlHelper.getGlTexture;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<EntityEspConfig, List<EntityRenderState>> overlayEntityStates = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<EntityRenderState>> outlineEntityStates = new IdentityHashMap<>();
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final Map<EntityEspConfig, List<EntityTypeVertexConsumerEntry>> overlayVertexConsumers = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<EntityTypeVertexConsumerEntry>> outlineVertexConsumers = new IdentityHashMap<>();
    private final Map<EntityScriptResultKey, EntityScriptResult> scriptResults = new HashMap<>();
    private boolean enabled = true;

    private EntityEsp() {
        Events.BeforeRenderWorld.add(this::onBeforeRenderWorld);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void captureEntityRenderState(Entity entity, EntityRenderState renderState) {
        if (mc.player == null || !EspGlobal.enabled || !enabled) {
            return;
        }

        boolean overlayFound = false;
        boolean outlineFound = false;

        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (!config.enabled) {
                continue;
            }

            if (!config.isValidEntity(entity)) {
                continue;
            }

            if (!overlayFound) {
                overlayFound = config.drawOverlay &&
                        entity.distanceToSqr(mc.player) < config.getOverlayMaxDistanceSqr() &&
                        !isOverlayDisabledFromScript(config, entity);
                if (overlayFound) {
                    List<EntityRenderState> states = overlayEntityStates.computeIfAbsent(config, _ -> new ArrayList<>());
                    states.add(renderState);
                    if (outlineFound) {
                        break;
                    }
                }
            }

            if (!outlineFound) {
                outlineFound = config.useModOutline() &&
                        entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr() &&
                        !isOutlineDisabledFromScript(config, entity);
                if (outlineFound) {
                    List<EntityRenderState> states = outlineEntityStates.computeIfAbsent(config, _ -> new ArrayList<>());
                    states.add(renderState);
                    if (overlayFound) {
                        break;
                    }
                }
            }
        }
    }

    public VertexConsumer getOutlineVertexConsumer(EntityEspConfig config, Identifier texture) {
        List<EntityTypeVertexConsumerEntry> entries = outlineVertexConsumers.computeIfAbsent(config, c -> new ArrayList<>());
        for (EntityTypeVertexConsumerEntry entry : entries) {
            if (entry.texture.equals(texture)) {
                return entry.consumer;
            }
        }

        BufferVertexConsumer consumer = new BufferVertexConsumer();
        entries.add(new EntityTypeVertexConsumerEntry(texture, consumer));
        return consumer;
    }

    public VertexConsumer getOverlayVertexConsumer(EntityEspConfig config, Identifier texture) {
        List<EntityTypeVertexConsumerEntry> entries = overlayVertexConsumers.computeIfAbsent(config, c -> new ArrayList<>());
        for (EntityTypeVertexConsumerEntry entry : entries) {
            if (entry.texture.equals(texture)) {
                return entry.consumer;
            }
        }

        BufferVertexConsumer consumer = new BufferVertexConsumer();
        entries.add(new EntityTypeVertexConsumerEntry(texture, consumer));
        return consumer;
    }

    public boolean shouldEntityGlow(Entity entity) {
        if (!enabled || !EspGlobal.enabled) {
            return false;
        }
        if (mc.player == null) {
            return false;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr()) {
                return !isOutlineDisabledFromScript(config, entity);
            }
        }
        return false;
    }

    public Integer getGlowColor(Entity entity) {
        if (!EspGlobal.enabled) {
            return null;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity)) {
                return config.glowColor.getRGB();
            }
        }
        return null;
    }

    public StylizedText getTitleOverride(EntityEspConfig config, Entity entity) {
        if (!config.scriptEnabled || config.script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.title;
        }

        return executeScript(config, entity).title;
    }

    private void onBeforeRenderWorld() {
//        overlayEntityStates.clear();
//        outlineEntityStates.clear();
        scriptResults.clear();
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        assert mc.level != null && mc.player != null;

        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        float partialTicks = event.getPartialTickTime();

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        MainFrameBuffer.bind();

        LineRenderer lineRenderer = RenderUtilities.instance.getLineRenderer();
        ThickLineRenderer thickLineRenderer = RenderUtilities.instance.getThickLineRenderer();

        lineRenderer.begin(event, false);
        thickLineRenderer.begin(event, false);

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        for (Entity entity : mc.level.entitiesForRendering()) {
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
                    c.drawOutline &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null && !isCollisionBoxDisabledFromScript(config, entity)) {
                Vec3 pos = entity.getPosition(partialTicks);
                AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);

                float r = config.outlineColor.getRed() / 255f;
                float g = config.outlineColor.getGreen() / 255f;
                float b = config.outlineColor.getBlue() / 255f;
                float a = config.outlineColor.getAlpha() / 255f;

                final int lineWidth = config.outlineWidth;
                if (lineWidth == 1) {
                    lineRenderer.cuboid(
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            r, g, b, a);
                } else {
                    thickLineRenderer.setWidth(lineWidth);
                    thickLineRenderer.cuboid(
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            r, g, b, a);
                }
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null && !isTracerDisabledFromScript(config, entity)) {
                Integer colorOverrideBoxed = getTracerColorOverride(config, entity);
                float r, g, b, a;
                if (colorOverrideBoxed == null) {
                    r = config.tracerColor.getRed() / 255f;
                    g = config.tracerColor.getGreen() / 255f;
                    b = config.tracerColor.getBlue() / 255f;
                    a = config.tracerColor.getAlpha() / 255f;
                } else {
                    int color = colorOverrideBoxed;
                    r = ColorUtils.r(color);
                    g = ColorUtils.g(color);
                    b = ColorUtils.b(color);
                    a = ColorUtils.a(color);
                }

                Vec3 pos = entity.getPosition(event.getPartialTickTime());
                final int lineWidth = config.tracerWidth;
                if (lineWidth == 1) {
                    lineRenderer.line(tracerX, tracerY, tracerZ, pos.x, pos.y, pos.z, r, g, b, a);
                } else {
                    thickLineRenderer.setWidth(lineWidth);
                    thickLineRenderer.line(tracerX, tracerY, tracerZ, pos.x, pos.y, pos.z, r, g, b, a);
                }
            }
        }

        lineRenderer.end();
        thickLineRenderer.end();

        /*drawOverlays(event);
        drawOutlines(event);*/

        drawOverlays2(list, event);

        overlayEntityStates.clear();
        outlineEntityStates.clear();
    }

    private RenderTarget renderTarget;
    private OverlayDrawProgram drawProgram;
    private RenderPipeline overlayPipeline;
    private GpuBuffer overlayColorBuffer;

    private void drawOverlays2(ImmutableList<EntityEspConfig> list, RenderWorldLastEvent event) {
        if (overlayEntityStates.isEmpty()) {
            return;
        }

        if (drawProgram == null) {
            drawProgram = new OverlayDrawProgram();
        }
        if (renderTarget == null) {
            WindowRenderState windowState = mc.gameRenderer.getGameRenderState().windowRenderState;
            renderTarget = new TextureTarget("[" + ModMain.MODID + "] EntityEsp", windowState.width, windowState.height, true);
        }
        if (overlayPipeline == null) {
            overlayPipeline = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/entity_overlay_blit"))
                    .withSampler("InSampler")
                    .withUniform("MyBlock", UniformType.UNIFORM_BUFFER)
                    .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "screenquad"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "blit_screen"))
                    .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.ENTITY_OUTLINE_BLIT), 7))
                    .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                    .build();
        }
        if (overlayColorBuffer == null) {
            overlayColorBuffer = RenderSystem.getDevice().createBuffer(() -> "Hello", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 16);
        }

        Vec3 cameraPos = event.getCameraPos();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        /**/RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1.0);

        PoseStack poseStack = new PoseStack();
        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
        for (EntityEspConfig config : list) {
            List<EntityRenderState> states = overlayEntityStates.get(config);
            if (states == null || states.isEmpty()) {
                continue;
            }

            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1.0);
            RenderSystem.outputColorTextureOverride = renderTarget.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = renderTarget.getDepthTextureView();

            submitNodeStorage.clear();

            for (EntityRenderState state : states) {
                int outlineColor = state.outlineColor;
                state.outlineColor = 0;
                renderDispatcher.submit(state, event.getCameraRenderState(), state.x - camX, state.y - camY, state.z - camZ, poseStack, submitNodeStorage);
                state.outlineColor = outlineColor;
            }

            OutlineCaptureBufferSource bufferSource = new OutlineCaptureBufferSource();
            OutlineBufferSource outlineBufferSource = new OutlineBufferSource();
            EmptyBufferSource emptyBufferSource = new EmptyBufferSource();

            FeatureRenderDispatcher dispatcher = new FeatureRenderDispatcher(
                    submitNodeStorage,
                    mc.getModelManager(),
                    bufferSource,
                    mc.getAtlasManager(),
                    outlineBufferSource,
                    emptyBufferSource,
                    mc.font,
                    mc.gameRenderer.getGameRenderState());

            dispatcher.renderAllFeatures();

            bufferSource.endBatch();

            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;

            /*renderTarget.blitAndBlendToTexture(mc.getMainRenderTarget().getColorTextureView());*/

            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer buffer = stack.malloc(16);
                buffer.putFloat(config.overlayColor.getRed() / 255f);
                buffer.putFloat(config.overlayColor.getGreen() / 255f);
                buffer.putFloat(config.overlayColor.getBlue() / 255f);
                buffer.putFloat(config.overlayColor.getAlpha() / 255f);
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(overlayColorBuffer.slice(), buffer.flip());
            }

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", mc.getMainRenderTarget().getColorTextureView(), OptionalInt.empty())) {
                renderPass.setPipeline(overlayPipeline);
                renderPass.bindTexture("InSampler", renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                renderPass.setUniform("MyBlock", overlayColorBuffer);
                renderPass.draw(0, 3);
            }
        }
    }

    private static final class OutlineCaptureBufferSource extends MultiBufferSource.BufferSource {

        public OutlineCaptureBufferSource() {
            super(new ByteBufferBuilder(1 << 16), Object2ObjectSortedMaps.emptyMap());
        }

        @Override
        public @NonNull VertexConsumer getBuffer(final RenderType renderType) {
            if (renderType.isOutline()) {
                return super.getBuffer(renderType);
            }

            return renderType.outline().map(super::getBuffer).orElse(EmptyVertexConsumer.instance);
        }
    }

    private static final class EmptyBufferSource extends MultiBufferSource.BufferSource {
        public EmptyBufferSource() {
            super(ByteBufferBuilder.exactlySized(4), Object2ObjectSortedMaps.emptyMap());
        }
    }

    private void drawOverlays(RenderWorldLastEvent event) {
        EntityOverlayRenderer renderer = RenderUtilities.instance.getEntityOverlayRenderer();
        for (EntityEspConfig config: overlayVertexConsumers.keySet()) {
            List<EntityTypeVertexConsumerEntry> entries = overlayVertexConsumers.get(config);
            if (entries.isEmpty()) {
                continue;
            }

            renderer.begin();

            for (EntityTypeVertexConsumerEntry entry: entries) {
                FloatList list = entry.consumer.list;
                if (list.size() == 0) {
                    continue;
                }

                int size = list.size();
                if (size % 20 != 0) {
                    continue; // invalid buffer, silently skip
                }

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
                renderer.renderBuffer(event.getMvp(), getGlTexture(texture.getTexture()).glId());
            }

            renderer.end(
                    config.overlayColor.getRed() / 255f,
                    config.overlayColor.getGreen() / 255f,
                    config.overlayColor.getBlue() / 255f,
                    config.overlayColor.getAlpha() / 255f);
        }

        overlayVertexConsumers.clear();
    }

    private void drawOutlines(RenderWorldLastEvent event) {
        EntityOutlineRenderer renderer = RenderUtilities.instance.getEntityOutlineRenderer();
        for (EntityEspConfig config: outlineVertexConsumers.keySet()) {
            List<EntityTypeVertexConsumerEntry> entries = outlineVertexConsumers.get(config);
            if (entries.isEmpty()) {
                continue;
            }

            renderer.begin();

            for (EntityTypeVertexConsumerEntry entry : entries) {
                FloatList list = entry.consumer.list;
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
                renderer.renderBuffer(event.getMvp(), getGlTexture(texture.getTexture()).glId());
            }

            renderer.end(
                    config.glowColor.getRed() / 255f,
                    config.glowColor.getGreen() / 255f,
                    config.glowColor.getBlue() / 255f,
                    config.glowColor.getAlpha() / 255f);
        }

        outlineVertexConsumers.clear();
    }

    private boolean isCollisionBoxDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.collisionBoxDisabled);
    }

    private boolean isTracerDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.tracerDisabled);
    }

    private boolean isOverlayDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.overlayDisabled);
    }

    private boolean isOutlineDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.outlineDisabled);
    }

    private Integer getTracerColorOverride(EntityEspConfig config, Entity entity) {
        if (!config.scriptEnabled || config.script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.tracerColorOverride;
        } else {
            return executeScript(config, entity).tracerColorOverride;
        }
    }

    private boolean getBooleanFromScript(EntityEspConfig config, Entity entity, Predicate<EntityScriptResult> predicate) {
        if (!config.scriptEnabled || config.script == null) {
            return false;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return predicate.test(result);
        }

        return predicate.test(executeScript(config, entity));
    }

    private EntityScriptResult executeScript(EntityEspConfig config, Entity entity) {
        assert config.script != null;

        EntityScriptResult result = new EntityScriptResult(entity.getId(), config);
        scriptResults.put(new EntityScriptResultKey(entity.getId(), config), result);
        config.script.accept(entity.getId(), new EntityEspEvent(result));
        return result;
    }

    public static class EntityScriptResult {

        public final int id;
        public final EntityEspConfig config;
        public boolean tracerDisabled;
        public boolean outlineDisabled;
        public boolean overlayDisabled;
        public boolean collisionBoxDisabled;
        public StylizedText title;
        public Integer tracerColorOverride;

        public EntityScriptResult(int id, EntityEspConfig config) {
            this.id = id;
            this.config = config;
        }
    }

    private static class EntityScriptResultKey {

        private final int id;
        private final EntityEspConfig config;

        public EntityScriptResultKey(int id, EntityEspConfig config) {
            this.id = id;
            this.config = config;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntityScriptResultKey other) {
                return other.id == id && other.config == config;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * id + config.hashCode();
        }
    }

    private static class BufferVertexConsumer implements VertexConsumer {

        private final FloatList list;

        public BufferVertexConsumer() {
            this.list = new FloatList();
        }

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            list.add(x);
            list.add(y);
            list.add(z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int i) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            list.add(u);
            list.add(v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int i, int j) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int i, int j) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float f, float g, float h) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setLineWidth(float f) {
            return this;
        }
    }

    public record EntityEspRenderState(EntityEspConfig outlineConfig, EntityEspConfig overlayConfig) {
        public static final EntityEspRenderState EMPTY = new EntityEspRenderState(null, null);
    }

    public record EntityTypeVertexConsumerEntry(Identifier texture, BufferVertexConsumer consumer) {}
}