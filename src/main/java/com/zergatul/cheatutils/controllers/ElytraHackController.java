package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

public class ElytraHackController {

    public static final ElytraHackController instance = new ElytraHackController();

    private final Minecraft mc = Minecraft.getInstance();

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
        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA) {
            return;
        }
        if (!mc.player.isFallFlying()) {
            return;
        }

        if (config.heightControl) {
            Vec3 delta = mc.player.getDeltaMovement();
            double heightControlShift = 0.2d;
            if (mc.options.keyJump.isDown()) {
                mc.player.setDeltaMovement(delta.x, delta.y + heightControlShift, delta.z);
            }
            if (mc.options.keyShift.isDown()) {
                mc.player.setDeltaMovement(delta.x, delta.y - heightControlShift, delta.z);
            }
        }
        if (config.speedControl) {
            Vec3 delta = mc.player.getDeltaMovement();
            float yaw = (float)Math.toRadians(mc.player.getYRot());
            Vec3 forward = new Vec3(-Math.sin(yaw) * 0.05, 0, Math.cos(yaw) * 0.05);
            if (mc.options.keyUp.isDown()) {
                mc.player.setDeltaMovement(delta.add(forward));
            }
            if (mc.options.keyDown.isDown()) {
                mc.player.setDeltaMovement(delta.subtract(forward));
            }
        }
        if (config.speedLimitEnabled) {
            Vec3 delta = mc.player.getDeltaMovement();
            double speed = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
            if (speed > config.speedLimit / 20) {
                double factor = config.speedLimit / 20 / speed;
                mc.player.setDeltaMovement(delta.x * factor, delta.y * factor, delta.z * factor);
            }
        }
        if (config.horizontalSpeedLimitEnabled) {
            Vec3 delta = mc.player.getDeltaMovement();
            double horSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            if (horSpeed > config.horizontalSpeedLimit / 20) {
                double factor = config.horizontalSpeedLimit / 20 / horSpeed;
                mc.player.setDeltaMovement(delta.x * factor, delta.y, delta.z * factor);
            }
        }
    }
}
