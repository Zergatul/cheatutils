package com.zergatul.cheatutils.modules.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.render.ItemRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArmorOverlay {

    public static final ArmorOverlay instance = new ArmorOverlay();

    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlay() {

    }

    public boolean render(PoseStack poseStack, int left, int top) {
        if (mc.player == null) {
            return false;
        }

        if (!ConfigStore.instance.getConfig().armorOverlayConfig.enabled) {
            return false;
        }

        List<ItemStack> armor = mc.player.getInventory().armor;
        ItemRenderHelper.renderItem(poseStack, mc.player, armor.get(3), left, top);
        left += 16;
        ItemRenderHelper.renderItem(poseStack, mc.player, armor.get(2), left, top);
        left += 16;
        ItemRenderHelper.renderItem(poseStack, mc.player, armor.get(1), left, top);
        left += 16;
        ItemRenderHelper.renderItem(poseStack, mc.player, armor.get(0), left, top);

        return true;
    }
}