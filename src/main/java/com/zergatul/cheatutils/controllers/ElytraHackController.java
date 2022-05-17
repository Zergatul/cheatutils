package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElytraHackController {

    public static final ElytraHackController instance = new ElytraHackController();

    private final Minecraft mc = Minecraft.getInstance();
    private double heightControlShift = 0.2d;

    private ElytraHackController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
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
            if (config.horizontalFlight) {
                Vec3 delta = mc.player.getDeltaMovement();
                //double horizontalDelta2 = delta.x * delta.x + delta.z * delta.z;
                //horizontalDelta2 > 0.8d
                if (mc.options.keyShift.isDown()) {
                    mc.player.setDeltaMovement(delta.x, 0, delta.z);
                }
            }
        }
    }
}
