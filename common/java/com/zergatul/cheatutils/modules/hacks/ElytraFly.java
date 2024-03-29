package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraFly implements Module {

    public static final ElytraFly instance = new ElytraFly();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean applySpeedLimit;
    private boolean applyFly;

    private ElytraFly() {
        Events.ClientTickStart.add(this::onClientTickStart);
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public Vec3 onBeforeMove(MoverType type, Vec3 delta) {
        if (type == MoverType.SELF && applySpeedLimit) {
            ElytraHackConfig config = ConfigStore.instance.getConfig().elytraHackConfig;
            double speed = delta.length() * 20;
            if (speed > config.maxSpeed) {
                delta = delta.normalize().scale(config.maxSpeed / 20);
            }

            mc.player.setDeltaMovement(delta);
        }

        return delta;
    }

    public void onBeforeAiStep() {
        ElytraHackConfig config = ConfigStore.instance.getConfig().elytraHackConfig;
        if (applyFly) {
            mc.player.stopFallFlying();
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlyingSpeed((float) (config.maxSpeed / 100));
        }
    }

    public float onModifyFlyingHorizontalMultiplier(float value) {
        return applyFly ? 1 / 0.6f : value;
    }

    public void onAfterAiStep() {
        if (applyFly) {
            if (mc.player.getAbilities().flying) {
                mc.player.startFallFlying();
            }
            mc.player.getAbilities().flying = false;
            applyFly = false;
        }
    }

    public boolean shouldPlaySound() {
        return !applyFly;
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

        switch (config.method) {
            case ElytraHackConfig.CREATIVE_FLY -> {
                applyFly = true;
                applySpeedLimit = true;
            }
            default -> {
                Vec3 delta = mc.player.getDeltaMovement();
                Input input = mc.player.input;
                if (input.jumping) {
                    delta = delta.add(0, config.vanillaFlyVerticalAcceleration / 20, 0);
                }
                if (input.shiftKeyDown) {
                    delta = delta.subtract(0, config.vanillaFlyVerticalAcceleration / 20, 0);
                }

                float yaw = (float) Math.toRadians(mc.player.getYRot());
                Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)).scale(config.vanillaFlyHorizontalAcceleration / 20);
                if (input.up) {
                    delta = delta.add(forward);
                }
                if (input.down) {
                    delta = delta.subtract(forward);
                }
                mc.player.setDeltaMovement(delta);
                applySpeedLimit = true;
            }
        }
    }

    private void onClientTickEnd() {
        applySpeedLimit = false;
    }
}