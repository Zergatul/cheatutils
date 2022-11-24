package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class ElytraHackController {

    public static final ElytraHackController instance = new ElytraHackController();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private ElytraHackController() {
        ModApiWrapper.ClientTickStart.add(this::onClientTickStart);
    }

    private void onClientTickStart() {
        ElytraHackConfig config = ConfigStore.instance.getConfig().elytraHackConfig;
        if (!config.enabled) {
            return;
        }
        if (mc.player == null) {
            return;
        }
        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA) {
            return;
        }
        if (!mc.player.isFallFlying()) {
            return;
        }

        if (config.heightControl) {
            Vec3d delta = mc.player.getVelocity();
            double heightControlShift = 0.2d;
            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(delta.x, delta.y + heightControlShift, delta.z);
            }
            if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(delta.x, delta.y - heightControlShift, delta.z);
            }
        }
        if (config.speedControl) {
            Vec3d delta = mc.player.getVelocity();
            float yaw = (float)Math.toRadians(mc.player.getYaw());
            Vec3d forward = new Vec3d(-Math.sin(yaw) * 0.05, 0, Math.cos(yaw) * 0.05);
            if (mc.options.forwardKey.isPressed()) {
                mc.player.setVelocity(delta.add(forward));
            }
            if (mc.options.backKey.isPressed()) {
                mc.player.setVelocity(delta.subtract(forward));
            }
        }
        if (config.speedLimitEnabled) {
            Vec3d delta = mc.player.getVelocity();
            double speed = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
            if (speed > config.speedLimit / 20) {
                double factor = config.speedLimit / 20 / speed;
                mc.player.setVelocity(delta.x * factor, delta.y * factor, delta.z * factor);
            }
        }
        if (config.horizontalSpeedLimitEnabled) {
            Vec3d delta = mc.player.getVelocity();
            double horSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            if (horSpeed > config.horizontalSpeedLimit / 20) {
                double factor = config.horizontalSpeedLimit / 20 / horSpeed;
                mc.player.setVelocity(delta.x * factor, delta.y, delta.z * factor);
            }
        }
    }
}