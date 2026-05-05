package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CacheItemRenderer {

    public static final CacheItemRenderer instance = new CacheItemRenderer();

    private final Minecraft mc = Minecraft.getInstance();
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final Queue<FreeSlot> freeSlots;
    private final Map<Object, OccupiedSlot> slots;
    private final List<Object> animated;
    private final LinkedList<OccupiedSlot> cacheList;
    private final Map<ItemRequest, TrackingItemStackRenderState> requestedItems;
    private final Projection projection;
    private final ProjectionMatrixBuffer projectionMatrixBuffer;
    private GpuTexture texture;
    private GpuTextureView textureView;
    private GpuTexture depthTexture;
    private GpuTextureView depthTextureView;
    private int slotSize;
    private int textureSize;
    private int guiScale;
    private long frame;

    private CacheItemRenderer() {
        this.freeSlots = new ArrayDeque<>();
        this.slots = new HashMap<>();
        this.animated = new ArrayList<>();
        this.cacheList = new LinkedList<>();
        this.requestedItems = new HashMap<>();
        this.projection = new Projection();
        this.projectionMatrixBuffer = new ProjectionMatrixBuffer(ModMain.MODID + " items");
        this.slotSize = 4; // 4x4 by default
    }

    public void beginFrame(int guiScale) {
        if (this.guiScale != guiScale) {
            invalidate();
        }

        this.guiScale = guiScale;
        this.requestedItems.clear();
        this.frame++;

        for (Object identity : animated) {
            OccupiedSlot slot = slots.get(identity);
            if (slot == null) {
                continue;
            }

            cacheList.remove(slot);
            slots.remove(identity);
            freeSlots.add(new FreeSlot(slot.x, slot.y));
        }

        animated.clear();
    }

    public void requestItem(@Nullable ItemOwner owner, ItemStack itemStack) {
        TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GUI, mc.level, owner, 0);
        requestedItems.put(new ItemRequest(owner, itemStack), renderState);
    }

    public void updateAtlas() {
        if (requestedItems.isEmpty()) {
            return;
        }

        if (texture == null) {
            textureSize = slotSize * 16 * guiScale;
            GpuDevice device = RenderSystem.getDevice();
            texture = device.createTexture(ModMain.MODID + " items atlas", 13, GpuFormat.RGBA8_UNORM, textureSize, textureSize, 1, 1);
            textureView = device.createTextureView(texture);
            depthTexture = device.createTexture(ModMain.MODID + " items atlas depth", 9, GpuFormat.D32_FLOAT, textureSize, textureSize, 1, 1);
            depthTextureView = device.createTextureView(depthTexture);

            for (int y = 0; y < slotSize; y++) {
                for (int x = 0; x < slotSize; x++) {
                    freeSlots.add(new FreeSlot(x, y));
                }
            }
        }

        for (TrackingItemStackRenderState renderState : requestedItems.values()) {
            Object identity = renderState.getModelIdentity();
            OccupiedSlot existing = slots.get(identity);
            if (existing != null) {
                existing.lastUsedFrame = frame;
                cacheList.remove(existing);
                cacheList.addFirst(existing);
                continue;
            }

            int slotX, slotY;
            if (freeSlots.isEmpty()) {
                if (cacheList.getLast().lastUsedFrame < frame) {
                    // drop cache
                    OccupiedSlot last = cacheList.removeLast();
                    slots.remove(last.identity);
                    slotX = last.x;
                    slotY = last.y;
                } else {
                    slotSize *= 2;
                    invalidate();
                    updateAtlas();
                    continue;
                }
            } else {
                FreeSlot freeSlot = freeSlots.remove();
                slotX = freeSlot.x;
                slotY = freeSlot.y;
            }

            drawSlot(slotX, slotY, renderState);
            OccupiedSlot slot = new OccupiedSlot(slotX, slotY, identity, frame);
            slots.put(identity, slot);
            cacheList.addFirst(slot);

            if (renderState.isAnimated()) {
                animated.add(identity);
            }
        }
    }

    public @Nullable AtlasSlot getTexture(@Nullable ItemOwner owner, ItemStack itemStack) {
        TrackingItemStackRenderState renderState = requestedItems.get(new ItemRequest(owner, itemStack));
        if (renderState == null) {
            return null;
        }

        OccupiedSlot slot = slots.get(renderState.getModelIdentity());
        if (slot == null) {
            return null;
        }

        int slotSize = getSlotSize();
        int x = slot.x * slotSize;
        int y = slot.y * slotSize;
        float u0 = (float)x / textureSize;
        float u1 = (float)(x + slotSize) / textureSize;
        float v0 = 1f - (float)y / textureSize;
        float v1 = 1f - (float)(y + slotSize) / textureSize;
        return new AtlasSlot(textureView, u0, v0, u1, v1);
    }

    // copied from GuiItemAtlas.drawToSlot
    private void drawSlot(int slotX, int slotY, ItemStackRenderState renderState) {
        int slotSize = getSlotSize();
        int halfSlot = slotSize / 2;
        int left = slotX * slotSize;
        int top = slotY * slotSize;
        int bottom = top + slotSize;

        GpuDevice device = RenderSystem.getDevice();
        device.createCommandEncoder().clearColorAndDepthTextures(
                texture, GuiRenderer.CLEAR_COLOR, depthTexture, 0.0,
                left, textureSize - bottom, slotSize, slotSize);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(left + halfSlot, top + halfSlot, 0.0F);
        poseStack.scale(slotSize, -slotSize, slotSize);
        RenderSystem.outputColorTextureOverride = textureView;
        RenderSystem.outputDepthTextureOverride = depthTextureView;
        projection.setupOrtho(-1000.0F, 1000.0F, textureSize, textureSize, true);
        RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.enableScissorForRenderTypeDraws(left, textureSize - bottom, slotSize, slotSize);
        Lighting.Entry lighting = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
        mc.gameRenderer.lighting().setupFor(lighting);
        renderState.submit(poseStack, submitNodeStorage, 15728880, OverlayTexture.NO_OVERLAY, 0);
        mc.gameRenderer.featureRenderDispatcher().renderAllFeatures(submitNodeStorage);
        RenderSystem.disableScissorForRenderTypeDraws();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        poseStack.popPose();
    }

    private void invalidate() {
        if (texture == null) {
            return;
        }

        textureView.close();
        texture.close();
        texture = null;
        textureView = null;

        depthTextureView.close();
        depthTexture.close();
        depthTexture = null;
        depthTextureView = null;

        freeSlots.clear();
        slots.clear();
        animated.clear();
        cacheList.clear();
    }

    private int getSlotSize() {
        return guiScale * 16;
    }

    private record FreeSlot(int x, int y) {}

    private static class OccupiedSlot {

        public final int x;
        public final int y;
        public final Object identity;
        public long lastUsedFrame;

        public OccupiedSlot(int x, int y, Object identity, long lastUsedFrame) {
            this.x = x;
            this.y = y;
            this.identity = identity;
            this.lastUsedFrame = lastUsedFrame;
        }
    }

    private record ItemRequest(ItemOwner owner, ItemStack itemStack) {}

    public record AtlasSlot(GpuTextureView textureView, float u0, float v0, float u1, float v1) {}
}