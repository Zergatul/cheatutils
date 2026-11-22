package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AntiHungerConfig;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiHunger implements Module {

    public static final AntiHunger INSTANCE = new AntiHunger();

    private final SettingHandle<AntiHungerConfig> settings = Settings.get(AntiHungerConfig.class);
    private final Minecraft mc = Minecraft.getInstance();

    private AntiHunger() {
        Events.BeforeSendPacketClientThread.add(this::onClientPacket);

        WebApiRegistry.INSTANCE.register("anti-hunger", settings, AntiHungerConfig.class);
    }

    public SettingHandle<AntiHungerConfig> getSettings() {
        return settings;
    }

    private boolean onClientPacket(Packet<?> packet) {
        if (packet instanceof ServerboundMovePlayerPacket) {
            AntiHungerConfig config = settings.get();
            if (!config.enabled()) {
                return true;
            }
            if (NoFall.INSTANCE.isActive()) {
                return true;
            }
            if (FlyHack.INSTANCE.getSettings().get().enabled()) {
                return true;
            }
            LocalPlayer player = mc.player;
            if (player == null) {
                return true;
            }
            if (player.isFallFlying()) {
                return true;
            }
            ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(false);
        }
        return true;
    }
}