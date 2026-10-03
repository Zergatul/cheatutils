package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ArmorOverlayConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorOverlay {

    public static final ArmorOverlay instance = new ArmorOverlay();

    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlay() {
        WebApiRegistry.INSTANCE.register(new WebApi());
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

    private static final class WebApi extends SimpleModuleConfigWebApi<ArmorOverlayConfig> {

        public WebApi() {
            super("armor-overlay", ArmorOverlayConfig .class);
        }

        @Override
        protected ArmorOverlayConfig getConfig() {
            return ConfigStore.instance.getConfig().armorOverlayConfig;
        }

        @Override
        protected void setConfig(ArmorOverlayConfig config) {
            ConfigStore.instance.getConfig().armorOverlayConfig = config;
        }
    }
}