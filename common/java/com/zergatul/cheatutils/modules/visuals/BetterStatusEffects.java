package com.zergatul.cheatutils.modules.visuals;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.Primitives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.Collection;
import java.util.List;

public class BetterStatusEffects implements Module {

    public static final BetterStatusEffects instance = new BetterStatusEffects();

    private final Minecraft mc = Minecraft.getInstance();

    private BetterStatusEffects() {

    }

    public boolean render(PoseStack poseStack, int screenWidth, int y) {
        if (mc.player == null) {
            return false;
        }

        if (!ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            return false;
        }

        Collection<MobEffectInstance> collection = mc.player.getActiveEffects();
        if (collection.isEmpty()) {
            return false;
        }

        MobEffectTextureManager manager = mc.getMobEffectTextures();

        int left = (screenWidth - collection.size() * 25) / 2;

        List<MobEffectInstance> sorted = Ordering.natural().sortedCopy(collection);
        for (int i = 0; i < sorted.size(); i++) {
            MobEffectInstance effectInstance = sorted.get(i);

            RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
            if (effectInstance.isAmbient()) {
                GuiComponent.blit(poseStack, left, y, 199, 165, 166, 24, 24, 256, 256);
            } else {
                GuiComponent.blit(poseStack, left, y, 199, 141, 166, 24, 24, 256, 256);
            }

            TextureAtlasSprite textureatlassprite = manager.get(effectInstance.getEffect());
            RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            GuiComponent.blit(poseStack, left + 3, y + 3, 200, 18, 18, textureatlassprite);

            String duration = MobEffectUtil.formatDuration(effectInstance, 1).getString();
            if (duration.startsWith("00")) {
                duration = duration.substring(1);
            }
            if (!duration.startsWith("0:") && duration.startsWith("0")) {
                duration = duration.substring(1);
            }
            int width = mc.font.width(duration);
            int textLeft = left + (24 - width) / 2;
            int textTop = y + 25;
            Primitives.fill(poseStack, textLeft, textTop, textLeft + width, textTop + mc.font.lineHeight, -1873784752);
            mc.font.draw(poseStack, duration, textLeft, textTop, 16777215);

            left += 25;
        }

        return true;
    }
}
