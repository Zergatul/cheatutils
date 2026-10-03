package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.AntiHungerConfig;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiHunger implements Module {

    public static final AntiHunger instance = new AntiHunger();

    private AntiHunger() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);

        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            if (!ConfigStore.instance.getConfig().antiHungerConfig.enabled) {
                return;
            }
            if (NoFall.instance.isActive()) {
                return;
            }
            if (ConfigStore.instance.getConfig().flyHackConfig.enabled) {
                return;
            }
            if (Minecraft.getInstance().player.isFallFlying()) {
                return;
            }
            ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(false);
        }
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<AntiHungerConfig> {

        public WebApi() {
            super("anti-hunger", AntiHungerConfig.class);
        }

        @Override
        protected AntiHungerConfig getConfig() {
            return ConfigStore.instance.getConfig().antiHungerConfig;
        }

        @Override
        protected void setConfig(AntiHungerConfig config) {
            ConfigStore.instance.getConfig().antiHungerConfig = config;
        }
    }
}