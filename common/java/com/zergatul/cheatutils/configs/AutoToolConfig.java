package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoToolConfig extends ModuleConfig implements Sanitizable {

    public static final String MODE_HOTBAR = "HOTBAR";
    public static final String MODE_INVENTORY = "INVENTORY";

    public static final String PRIORITY_SPEED = "SPEED";
    public static final String PRIORITY_DROP = "DROP";

    public String mode;
    public String priority;
    public int slot;
    public int minDurability;

    public AutoToolConfig() {
        mode = MODE_HOTBAR;
        priority = PRIORITY_DROP;
        minDurability = 10;
    }

    @Override
    public void sanitize() {
        if (!mode.equals(MODE_HOTBAR) && !mode.equals(MODE_INVENTORY)) {
            mode = MODE_HOTBAR;
        }
        if (!PRIORITY_SPEED.equals(priority) && !PRIORITY_DROP.equals(priority)) {
            priority = PRIORITY_DROP;
        }
        slot = MathUtils.clamp(slot, 1, 9);
        minDurability = MathUtils.clamp(minDurability, 0, 1000);
    }
}