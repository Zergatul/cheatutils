package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

public class EntityInteraction {

    private final static Minecraft mc = Minecraft.getInstance();

    public static void interact(Entity entity) {
        if (mc.gameMode == null) {
            return;
        }
        if (mc.player == null) {
            return;
        }
        mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}