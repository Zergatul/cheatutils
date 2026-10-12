package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ArmorOverlayConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorOverlay {

    public static final ArmorOverlay instance = new ArmorOverlay();

    private final ModuleConfigSettingHandle<ArmorOverlayConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().armorOverlayConfig,
            config -> ConfigStore.instance.getConfig().armorOverlayConfig = config);
    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlay() {
        WebApiRegistry.INSTANCE.register("armor-overlay", settings, ArmorOverlayConfig.class);
    }

    public ModuleConfigSettingHandle<ArmorOverlayConfig> getSettings() {
        return settings;
    }

    public void render(GuiGraphicsExtractor graphics, Player player, int left, int top) {
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.HEAD), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.CHEST), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.LEGS), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.FEET), left, top);
    }

    private void renderItem(GuiGraphicsExtractor graphics, ItemStack itemStack, int left, int top) {
        graphics.item(itemStack, left, top);
        graphics.itemDecorations(mc.font, itemStack, left, top);
    }
}