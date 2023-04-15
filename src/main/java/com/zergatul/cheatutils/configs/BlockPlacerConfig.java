package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockPlacerConfig extends ModuleConfig implements ValidatableConfig {

    public double maxRange;
    public int[] autoSelectSlots;
    public boolean attachToAir;
    public boolean useShift;

    protected BlockPlacerConfig() {
        maxRange = 5;
        autoSelectSlots = new int[] { 9 };
    }

    public void copyTo(ScriptedBlockPlacerConfig other) {
        super.copyTo(other);
        other.maxRange = maxRange;
        other.autoSelectSlots = autoSelectSlots;
        other.attachToAir = attachToAir;
        other.useShift = useShift;
    }

    @Override
    public void validate() {
        maxRange = MathUtils.clamp(maxRange, 1, 10);
        validateAutoSelectSlots();
    }

    private void validateAutoSelectSlots() {
        if (autoSelectSlots == null) {
            autoSelectSlots = new int[0];
        } else {
            boolean[] slots = new boolean[9];
            for (int i = 0; i < autoSelectSlots.length; i++) {
                int slot = autoSelectSlots[i] - 1;
                if (0 <= slot && slot < 9) {
                    slots[slot] = true;
                }
            }

            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                if (slots[i]) {
                    list.add(i + 1);
                }
            }

            autoSelectSlots = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                autoSelectSlots[i] = list.get(i);
            }
        }
    }
}