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
    private boolean skip;

    public void skipAttack() {
        skip = true;
    }


    private AutoCriticals() {
        skip = false;
        Events.BeforeAttack.add(this::onBeforeAttack);
    }


    private void onBeforeAttack(BeforeAttackEvent event) {
        AutoCriticalsConfig config = ConfigStore.instance.getConfig().autoCriticalsConfig;

        //This needs to come before enable check, else it will queue this for future situation incorrectly
        if (skip) {
            skip = false;
            return;
        }

        if (config.enabled) {
            if (mc.level == null) {
                return;
            }
            if (mc.player == null) {
                return;
            }


            if (config.onlyOnGround && !mc.player.onGround()) {
                return;
            }


            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
            if (mc.player.fallDistance > 0.1D || mc.player.isFallFlying()) {//If player already falling
                return;
            }

            Vec3 PrevPos = mc.player.position();

            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 0.0625D, PrevPos.z, true, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y + 1.1E-5D, PrevPos.z, false, false));
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Pos(PrevPos.x, PrevPos.y, PrevPos.z, false, false));
        }
    }
}