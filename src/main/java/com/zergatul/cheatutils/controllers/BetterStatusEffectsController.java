package com.zergatul.cheatutils.controllers;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.render.Primitives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;


import java.util.Collection;
import java.util.List;

public class BetterStatusEffectsController implements IGuiOverlay {

    public static final BetterStatusEffectsController instance = new BetterStatusEffectsController();

    private final Minecraft mc = Minecraft.getInstance();

    private BetterStatusEffectsController() {

    }

    public void onRegister(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("betterstatus", this);
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (mc.player == null) {
            return;
        }
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements() && ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            Collection<MobEffectInstance> collection = mc.player.getActiveEffects();
            if (collection.isEmpty()) {
                return;
            }

            gui.setupOverlayRenderState(true, false);
            MobEffectTextureManager manager = mc.getMobEffectTextures();

            int height = Math.max(gui.leftHeight, gui.rightHeight) + 24 + mc.font.lineHeight;
            int top = screenHeight - height;
            gui.leftHeight = gui.rightHeight = height;
            int left = (screenWidth - collection.size() * 25) / 2;

            List<MobEffectInstance> sorted = Ordering.natural().sortedCopy(collection);
            for (int i = 0; i < sorted.size(); i++) {
                MobEffectInstance effectInstance = sorted.get(i);

                RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
                if (effectInstance.isAmbient()) {
                    GuiComponent.blit(poseStack, left, top, 199, 165, 166, 24, 24, 256, 256);
                } else {
                    GuiComponent.blit(poseStack, left, top, 199, 141, 166, 24, 24, 256, 256);
                }

                TextureAtlasSprite textureatlassprite = manager.get(effectInstance.getEffect());
                RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                GuiComponent.blit(poseStack, left + 3, top + 3, 200, 18, 18, textureatlassprite);

                Component duration = MobEffectUtil.formatDuration(effectInstance, 1);
                int width = mc.font.width(duration);
                int textLeft = left + (24 - width) / 2;
                int textTop = top + 25;
                Primitives.fill(poseStack, textLeft, textTop, textLeft + width, textTop + mc.font.lineHeight, -1873784752);
                mc.font.draw(poseStack, duration, textLeft, textTop, 16777215);

                left += 25;
            }
        }
    }
}