package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall implements Module {

    public static final NoFall instance = new NoFall();

    private final ModuleConfigSettingHandle<NoFallConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().noFallConfig,
            config -> ConfigStore.instance.getConfig().noFallConfig = config);
    private final Minecraft mc = Minecraft.getInstance();

    private NoFall() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);

        WebApiRegistry.INSTANCE.register("no-fall", settings, NoFallConfig.class);
    }

    public ModuleConfigSettingHandle<NoFallConfig> getSettings() {
        return settings;
    }

    public boolean isActive() {
        NoFallConfig config = settings.get();
        if (!config.enabled) {
            return false;
        }

        if (FlyHack.INSTANCE.getSettings().get().enabled()) {
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
}