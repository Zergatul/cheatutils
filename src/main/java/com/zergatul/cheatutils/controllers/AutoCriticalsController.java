package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoCriticalsController {

    public static final AutoCriticalsController instance = new AutoCriticalsController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private List<PlayerInteractEntityC2SPacket> processed = new ArrayList<>();

    private AutoCriticalsController() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (ConfigStore.instance.getConfig().autoCriticalsConfig.enabled) {
            if (args.packet instanceof PlayerInteractEntityC2SPacket packet) {
                if (processed.contains(packet)) {
                    processed.remove(packet);
                    return;
                }

                packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
                    @Override
                    public void interact(Hand hand) {

                    }

                    @Override
                    public void interactAt(Hand hand, Vec3d pos) {

                    }

                    @Override
                    public void attack() {
                        if (ConfigStore.instance.getConfig().autoCriticalsConfig.onlyOnGround && !mc.player.isOnGround()) {
                            return;
                        }

                        args.skip = true;
                        processed.add(packet);

                        double x = mc.player.getX();
                        double y = mc.player.getY();
                        double z = mc.player.getZ();

                        sendPositionPacket(x, y + 0.0625D, z, true);
                        sendPositionPacket(x, y, z, false);
                        sendPositionPacket(x, y + 1.1E-5D, z, false);
                        sendPositionPacket(x, y, z, false);
                        NetworkPacketsController.instance.sendPacket(packet);
                    }
                });
            }
        }
    }

    private void sendPositionPacket(double x, double y, double z, boolean isOnGround) {
        NetworkPacketsController.instance.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, isOnGround));
    }
}