package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.AutoCriticalsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class AutoCriticals {

    public static final AutoCriticals instance = new AutoCriticals();

    private final ModuleConfigSettingHandle<AutoCriticalsConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().autoCriticalsConfig,
            config -> ConfigStore.instance.getConfig().autoCriticalsConfig = config);
    private final Minecraft mc = Minecraft.getInstance();

    private AutoCriticals() {
        Events.BeforeAttack.add(this::onBeforeAttack, -1);

        WebApiRegistry.INSTANCE.register("auto-criticals", settings, AutoCriticalsConfig.class);
    }

    public ModuleConfigSettingHandle<AutoCriticalsConfig> getSettings() {
        return settings;
    }

    private void onBeforeAttack(BeforeAttackEvent event) {
        AutoCriticalsConfig config = settings.get();

        if (config.enabled) {
            if (mc.level == null) {
                return;
            }
            if (mc.player == null) {
                return;
            }
            // Hard Conditions not met, cannot do anything about it
            // Refer to `Player.class` -> boolean canCriticalAttack(Entity entity)
            // Refer to `Player.class` -> void attack(Entity entity)
            if (!((mc.player.getAttackStrengthScale(0.5F) > 0.9F) &&
                    (!mc.player.onClimbable()
                            && !mc.player.isInWater() && !mc.player.isMobilityRestricted() && !mc.player.isPassenger()
                            && mc.player.getLivingEntity().isAlive() && !mc.player.isSprinting()))
            ) {
                return;
            }

            // Soft Conditions we can cheat to validate, do nothing if its already valid case
            if (mc.player.fallDistance > 0 && !mc.player.onGround()) {
                return;
            }

            Vec3 PrevPos = mc.player.position();

            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundClientTickEndPacket());
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 0.0625D, PrevPos.z, true, false));
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundClientTickEndPacket());
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundClientTickEndPacket());
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 1.1E-5D, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundClientTickEndPacket());
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendBufferedPacket(new ServerboundClientTickEndPacket());
        }
    }
}