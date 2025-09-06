package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoToolConfig extends ModuleConfig implements ValidatableConfig {

    public static final String MODE_HOTBAR = "HOTBAR";
    public static final String MODE_INVENTORY = "INVENTORY";

    public String mode;
    public int slot;
    public int minDurability;

    public AutoToolConfig() {
        mode = MODE_HOTBAR;
        minDurability = 10;
    }

    @Override
    public void validate() {
        if (!mode.equals(MODE_HOTBAR) && !mode.equals(MODE_INVENTORY)) {
            mode = MODE_HOTBAR;
        }
        slot = MathUtils.clamp(slot, 1, 9);
        minDurability = MathUtils.clamp(minDurability, 0, 1000);
    }
}