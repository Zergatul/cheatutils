package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AntiHungerConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiHunger implements Module {

    public static final AntiHunger INSTANCE = new AntiHunger();

    private final SettingHandle2<AntiHungerConfig> settings = Settings.get(AntiHungerConfig.class);

    private AntiHunger() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);

        WebApiRegistry.INSTANCE.register("anti-hunger", settings, AntiHungerConfig.class);
    }

    public SettingHandle2<AntiHungerConfig> getSettings() {
        return settings;
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            AntiHungerConfig config = settings.get();
            if (!config.enabled()) {
                return;
            }
            if (NoFall.instance.isActive()) {
                return;
            }
            if (FlyHack.INSTANCE.getSettings().get().enabled()) {
                return;
            }
            if (Minecraft.getInstance().player.isFallFlying()) {
                return;
            }
            ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(false);
        }
    }
}