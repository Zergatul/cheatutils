package com.zergatul.cheatutils.configs;

public class AutoHotbarConfig extends ModuleConfig {
    public boolean refillSlot1;
    public boolean refillSlot2;
    public boolean refillSlot3;
    public boolean refillSlot4;
    public boolean refillSlot5;
    public boolean refillSlot6;
    public boolean refillSlot7;
    public boolean refillSlot8;
    public boolean refillSlot9;
    public boolean refillSlotOffhand;

    public boolean shouldRefill(int slot) {
        return switch (slot) {
            case 0 -> refillSlot1;
            case 1 -> refillSlot2;
            case 2 -> refillSlot3;
            case 3 -> refillSlot4;
            case 4 -> refillSlot5;
            case 5 -> refillSlot6;
            case 6 -> refillSlot7;
            case 7 -> refillSlot8;
            case 8 -> refillSlot9;
            default -> false;
        };
    }
}