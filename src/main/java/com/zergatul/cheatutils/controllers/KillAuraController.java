package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;
import com.zergatul.cheatutils.interfaces.ClientWorldMixinInterface;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class KillAuraController {

    public static final KillAuraController instance = new KillAuraController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private Entity target;

    private KillAuraController() {
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
        ModApiWrapper.addOnClientTickEnd(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        var config = ConfigStore.instance.getConfig().killAuraConfig;
        if (!config.active) {
            target = null;
            return;
        }

        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;

        if (player == null || world == null) {
            return;
        }

        if (world.getTime() % config.attackTickInterval != 0) {
            return;
        }

        target = null;
        int targetPriority = Integer.MAX_VALUE;
        double targetDistance2 = Double.MAX_VALUE;

        float maxRange2 = config.maxRange * config.maxRange;
        for (Entity entity: ((ClientWorldMixinInterface)mc.world).getEntityManager().getLookup().iterate()) {
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

            if (priority < targetPriority || (priority == targetPriority && distance2 < targetDistance2)) {
                target = entity;
                targetPriority = priority;
                targetDistance2 = distance2;
            }
        }

        if (target != null) {
            //logger.info("Found target {}", target.getClass().getName());
            FakeRotationController.instance.setServerRotation(target.getBoundingBox().getCenter());
        }
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

        //logger.info("Attacking {}", target.getClass().getName());
        ClientPlayerEntity player = mc.player;
        mc.interactionManager.attackEntity(player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        target = null;
    }
}