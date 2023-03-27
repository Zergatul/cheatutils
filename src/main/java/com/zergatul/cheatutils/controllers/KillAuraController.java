package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;
import com.zergatul.cheatutils.interfaces.ClientWorldMixinInterface;
import com.zergatul.cheatutils.utils.MathUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAuraController {

    public static final KillAuraController instance = new KillAuraController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private long ticks;
    private long lastAttackTick;
    private Entity target;

    private KillAuraController() {
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
        ModApiWrapper.ClientPlayerLoggingIn.add(this::onPlayerLoggingIn);
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.DimensionChange.add(this::onDimensionChange);
    }

    public void onEnabled() {
        lastAttackTick = 0;
    }

    private void onPlayerLoggingIn(ClientConnection connection) {
        ticks = 0;
        lastAttackTick = 0;
    }

    private void onDimensionChange() {
        lastAttackTick = 0;
    }

    private void onClientTickEnd() {
        ticks++;

        KillAuraConfig config = ConfigStore.instance.getConfig().killAuraConfig;
        if (!config.enabled) {
            target = null;
            return;
        }

        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;

        if (player == null || world == null) {
            return;
        }

        if (ticks - lastAttackTick < config.attackTickInterval) {
            return;
        }

        target = null;
        int targetPriority = Integer.MAX_VALUE;
        double targetDistance2 = Double.MAX_VALUE;

        float maxRange2 = config.maxRange * config.maxRange;
        for (Entity entity : ((ClientWorldMixinInterface)mc.world).getEntityManager().getLookup().iterate()) {
            double distance2 = player.squaredDistanceTo(entity);
            if (distance2 > maxRange2) {
                continue;
            }

            if (entity instanceof LivingEntity living) {
                if (!living.isAlive()) {
                    continue;
                }
            }

            int priority = getPriority(config, entity);
            if (priority < 0) {
                continue;
            }

            if (config.maxHorizontalAngle != null || config.maxVerticalAngle != null) {
                Vec3d attackPoint = getAttackPoint(entity);
                Vec3d eyePos = new Vec3d(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
                Vec3d diff = attackPoint.subtract(eyePos);
                double diffXZ = Math.sqrt(diff.x * diff.x + diff.y * diff.y);

                if (config.maxHorizontalAngle != null) {
                    double targetYRot = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90F;
                    double delta = MathUtils.deltaAngle180(targetYRot, player.getYaw());
                    if (delta > config.maxHorizontalAngle) {
                        continue;
                    }
                }

                if (config.maxVerticalAngle != null) {
                    double targetXRot = Math.toDegrees(-Math.atan2(diff.y, diffXZ));
                    double delta = MathUtils.deltaAngle180(targetXRot, player.getPitch());
                    if (delta > config.maxVerticalAngle) {
                        continue;
                    }
                }
            }

            if (priority < targetPriority || (priority == targetPriority && distance2 < targetDistance2)) {
                target = entity;
                targetPriority = priority;
                targetDistance2 = distance2;
            }
        }

        if (target != null) {
            FakeRotationController.instance.setServerRotation(getAttackPoint(target));
        }
    }

    private Vec3d getAttackPoint(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    private int getPriority(KillAuraConfig config, Entity entity) {
        int i = 0;
        for (KillAuraConfig.PriorityEntry entry: config.priorities) {
            if (entry.enabled && entry.predicate.test(entity)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void onAfterSendPosition() {
        if (target == null) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        mc.interactionManager.attackEntity(player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        target = null;

        lastAttackTick = ticks;
    }
}