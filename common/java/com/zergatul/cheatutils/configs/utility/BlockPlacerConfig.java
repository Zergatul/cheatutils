package com.zergatul.cheatutils.configs.utility;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.InteractionConfig;

import java.util.ArrayList;
import java.util.List;

public interface BlockPlacerConfig extends InteractionConfig {
    ImmutableList<Integer> getAutoSelectSlots();
    boolean shouldAttachToAir();
    boolean shouldUseShift();
    double getPlacementRate();

    static ImmutableList<Integer> sanitizeAutoSelectSlots(ImmutableList<Integer> list) {
        if (list == null) {
            return ImmutableList.empty();
        }

        boolean[] slots = new boolean[9];
        for (int slot : list) {
            if (1 <= slot && slot <= 9) {
                slots[slot - 1] = true;
            }
        }

        List<Integer> sanitized = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (slots[i]) {
                sanitized.add(i + 1);
            }
        }

        return new ImmutableList<>(sanitized);
    }
}