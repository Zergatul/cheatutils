package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.configs.modules.NoFallConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall implements Module {

    public static final NoFall INSTANCE = new NoFall();

    private final SettingHandle<NoFallConfig> settings = Settings.get(NoFallConfig.class);
    private final Minecraft mc = Minecraft.getInstance();

    private NoFall() {
        Events.BeforeSendPacket.add(this::onClientPacket);

        WebApiRegistry.INSTANCE.register("no-fall", settings, NoFallConfig.class);
    }

    public SettingHandle<NoFallConfig> getSettings() {
        return settings;
    }

    public boolean isActive() {
        NoFallConfig config = settings.get();
        if (!config.enabled()) {
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

    private boolean onClientPacket(Packet<?> packet) {
        if (packet instanceof ServerboundMovePlayerPacket) {
            if (isActive()) {
                ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(true);
            }
        }
        return true;
    }
}