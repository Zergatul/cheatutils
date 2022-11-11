package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KillAuraController {

    public static final KillAuraController instance = new KillAuraController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(KillAuraController.class);
    private Entity target;

    private KillAuraController() {
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
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

            if (world.getGameTime() % config.attackTickInterval != 0) {
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

                if (priority < targetPriotity || (priority == targetPriotity && distance2 < targetDistance2)) {
                    target = entity;
                    targetPriotity = priority;
                    targetDistance2 = distance2;
                }
            }

            if (target != null) {
                //logger.info("Found target {}", target.getClass().getName());
                FakeRotationController.instance.setServerRotation(target.getBoundingBox().getCenter());
            }
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
        LocalPlayer player = mc.player;
        mc.gameMode.attack(player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        target = null;
    }
}