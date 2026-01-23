package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.AutoCriticalsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;


public class AutoCriticals {

    public static final AutoCriticals instance = new AutoCriticals();

    private final Minecraft mc = Minecraft.getInstance();

    private AutoCriticals() {
        Events.BeforeAttack.add(this::onBeforeAttack, -1);
    }

    private void onBeforeAttack(BeforeAttackEvent event) {
        AutoCriticalsConfig config = ConfigStore.instance.getConfig().autoCriticalsConfig;

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
            ) return;

            // Soft Conditions we can cheat to validate, do nothing if its already valid case
            if (mc.player.fallDistance > 0 && !mc.player.onGround()) return;

            Vec3 PrevPos = mc.player.position();

            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 0.0625D, PrevPos.z, true, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 1.1E-5D, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
        }
    }
}