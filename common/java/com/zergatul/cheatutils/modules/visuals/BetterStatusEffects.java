package com.zergatul.cheatutils.modules.visuals;

import com.google.common.collect.Ordering;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.GuiAccessor;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;

public class BetterStatusEffects implements Module {

    public static final BetterStatusEffects instance = new BetterStatusEffects();

    private final Minecraft mc = Minecraft.getInstance();

    private BetterStatusEffects() {

    }

    public void render(GuiGraphicsExtractor graphics, Player player, int y) {
        if (!ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            return;
        }

        Collection<MobEffectInstance> collection = player.getActiveEffects();
        if (collection.isEmpty()) {
            return;
        }

        int left = (graphics.guiWidth() - collection.size() * 25) / 2;

        List<MobEffectInstance> sorted = Ordering.natural().sortedCopy(collection);
        for (int i = 0; i < sorted.size(); i++) {
            MobEffectInstance effectInstance = sorted.get(i);

            if (effectInstance.isAmbient()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiAccessor.getEffectBackgroundAmbientSprite_CU(), left, y, 24, 24);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiAccessor.getEffectBackgroundSprite_CU(), left, y, 24, 24);
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(effectInstance.getEffect()), left + 3, y + 3, 18, 18);

            String duration = MobEffectUtil.formatDuration(effectInstance, 1, mc.level.tickRateManager().tickrate()).getString();
            if (duration.startsWith("00")) {
                duration = duration.substring(1);
            }
            if (!duration.startsWith("0:") && duration.startsWith("0")) {
                duration = duration.substring(1);
            }
            int width = mc.font.width(duration);
            int textLeft = left + (24 - width) / 2;
            int textTop = y + 25;

            graphics.fill(
                    textLeft - 1,
                    textTop,
                    textLeft - 1 + width + 2,
                    textTop + mc.font.lineHeight,
                    -1873784752);
            graphics.text(mc.font, duration, textLeft, textTop, -1);

            left += 25;
        }
    }
}