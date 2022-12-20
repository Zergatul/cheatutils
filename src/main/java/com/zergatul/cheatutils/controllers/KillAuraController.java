package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KillAuraController {

    public static final KillAuraController instance = new KillAuraController();

    private final Minecraft mc = Minecraft.getInstance();
    private long lastAttackTick;
    private Entity target;

    private KillAuraController() {
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
    }

    @SubscribeEvent
    public void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
        lastAttackTick = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var config = ConfigStore.instance.getConfig().killAuraConfig;
            if (!config.active) {
                target = null;
                return;
            }

            LocalPlayer player = mc.player;
            ClientLevel world = mc.level;

            if (player == null || world == null) {
                return;
            }

            if (world.getGameTime() - lastAttackTick < config.attackTickInterval) {
                return;
            }

            target = null;
            int targetPriotity = Integer.MAX_VALUE;
            double targetDistance2 = Double.MAX_VALUE;

            float maxRange2 = config.maxRange * config.maxRange;
            for (Entity entity: world.entitiesForRendering()) {
                double distance2 = player.distanceToSqr(entity);
                if (distance2 > maxRange2) {
                    continue;
                }

                if (entity instanceof LivingEntity) {
                    var living = (LivingEntity) entity;
                    if (!living.isAlive()) {
                        continue;
                    }
                }

                int priority = getPriority(config, entity);
                if (priority < 0) {
                    continue;
                }

                if (config.maxHorizontalAngle != null || config.maxVerticalAngle != null) {
                    Vec3 attackPoint = getAttackPoint(entity);
                    Vec3 eyePos = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                    Vec3 diff = attackPoint.subtract(eyePos);
                    double diffXZ = Math.sqrt(diff.x * diff.x + diff.y * diff.y);

                    if (config.maxHorizontalAngle != null) {
                        double targetYRot = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90F;
                        double delta = MathUtils.deltaAngle180(targetYRot, player.getYRot());
                        if (delta > config.maxHorizontalAngle) {
                            continue;
                        }
                    }

                    if (config.maxVerticalAngle != null) {
                        double targetXRot = Math.toDegrees(-Math.atan2(diff.y, diffXZ));
                        double delta = MathUtils.deltaAngle180(targetXRot, player.getXRot());
                        if (delta > config.maxVerticalAngle) {
                            continue;
                        }
                    }
                }

                if (priority < targetPriotity || (priority == targetPriotity && distance2 < targetDistance2)) {
                    target = entity;
                    targetPriotity = priority;
                    targetDistance2 = distance2;
                }
            }

            if (target != null) {
                FakeRotationController.instance.setServerRotation(getAttackPoint(target));
            }
        }
    }

    private Vec3 getAttackPoint(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    private int getPriority(KillAuraConfig config, Entity entity) {
        for (int i = 0; i < config.priorities.size(); i++) {
            var entry = config.priorities.get(i);
            if (entry.clazz.isInstance(entity)) {
                if (entry.predicate != null) {
                    if (!entry.predicate.test(entity, mc.player)) {
                        continue;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    private void onAfterSendPosition() {
        if (target == null) {
            return;
        }

        LocalPlayer player = mc.player;
        ClientLevel world = mc.level;
        mc.gameMode.attack(player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        target = null;

        lastAttackTick = world.getGameTime();
    }
}