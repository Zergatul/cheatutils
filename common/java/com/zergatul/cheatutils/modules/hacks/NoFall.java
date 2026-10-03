package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall implements Module {

    public static final NoFall instance = new NoFall();

    private final Minecraft mc = Minecraft.getInstance();

    private NoFall() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);

        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public boolean isActive() {
        NoFallConfig config = ConfigStore.instance.getConfig().noFallConfig;
        if (!config.enabled) {
            return false;
        }

        if (ConfigStore.instance.getConfig().flyHackConfig.enabled) {
            return false;
        }

        if (mc.player == null) {
            return false;
        }

        if (mc.player.isFallFlying()) {
            // flying with elytra
            return false;
        }

        return mc.player.getDeltaMovement().y < -0.5;
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            if (isActive()) {
                ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(true);
            }
        }
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<NoFallConfig> {

        public WebApi() {
            super("no-fall", NoFallConfig.class);
        }

        @Override
        protected NoFallConfig getConfig() {
            return ConfigStore.instance.getConfig().noFallConfig;
        }

        @Override
        protected void setConfig(NoFallConfig config) {
            ConfigStore.instance.getConfig().noFallConfig = config;
        }
    }
}