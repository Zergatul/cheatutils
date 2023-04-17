package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.accessors.ServerboundMovePlayerPacketAccessor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall implements Module {

    public static final NoFall instance = new NoFall();

    private final Minecraft mc = Minecraft.getInstance();
    //private long lastPacketJumpTime = 0;

    private NoFall() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            /*if (packet instanceof JumpPacket) {
                return;
            }*/

            NoFallConfig config = ConfigStore.instance.getConfig().noFallConfig;
            if (!config.enabled) {
                return;
            }

            /*if (config.method.equals(NoFallConfig.METHOD_ON_GROUND))*/ {
                if (ConfigStore.instance.getConfig().flyHackConfig.enabled) {
                    return;
                }

                if (mc.player.isFallFlying()) {
                    // flying with elytra
                    return;
                }

                if (mc.player.getDeltaMovement().y < -0.5) {
                    ((ServerboundMovePlayerPacketAccessor) packet).setOnGround_CU(true);
                }
            }

            /*if (config.method.equals(NoFallConfig.METHOD_PACKET_JUMP)) {
                if (packet.hasPosition() && !packet.isOnGround()) {
                    //long time = System.nanoTime();
                    //if (lastPacketJumpTime == 0 || time - lastPacketJumpTime >= 100000000) { // 100ms
                        //lastPacketJumpTime = time;
                    JumpPacket p;
                    NetworkPacketsController.instance.sendPacket(p = new JumpPacket(
                            packet.getX(0),
                            packet.getY(0) + 0.0001,
                            packet.getZ(0),
                            packet.isOnGround()));
                    //}

                    NetworkPacketsController.instance.sendPacket(p = new JumpPacket(
                            packet.getX(0),
                            packet.getY(0),
                            packet.getZ(0),
                            packet.isOnGround()));

                    System.out.println("MiniJump > " + p.getY(0));
                }
            }*/
        }
    }

    /*private static class JumpPacket extends ServerboundMovePlayerPacket.Pos {

        public JumpPacket(double x, double y, double z, boolean onGround) {
            super(x, y, z, onGround);
        }
    }*/
}