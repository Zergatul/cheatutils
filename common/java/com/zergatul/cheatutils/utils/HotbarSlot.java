package com.zergatul.cheatutils.utils;

import net.minecraft.world.InteractionHand;

public class HotbarSlot {

    private final InteractionHand hand;
    private final int slot;

    private HotbarSlot(InteractionHand hand, int slot) {
        this.hand = hand;
        this.slot = slot;
    }

    public static HotbarSlot createMainHand(int slot) {
        return new HotbarSlot(InteractionHand.MAIN_HAND, slot);
    }

    public static HotbarSlot createOffHand() {
        return new HotbarSlot(InteractionHand.OFF_HAND, -1);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public int getSlot() {
        return slot;
    }
}