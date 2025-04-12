package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public record SchematicaOutputData(int width, int height, int length, List<BlockState> palette, int[] blocks) {

    public SchematicaOutputData optimized() {
        int size = palette.size();
        int[] counts = new int[size];
        for (int block : blocks) {
            counts[block]++;
        }

        SortEntry[] entries = new SortEntry[size];
        for (int i = 0; i < size; i++) {
            entries[i] = new SortEntry(palette.get(i), i, counts[i]);
        }
        Arrays.sort(entries, 1, size, (e1, e2) -> e2.count - e1.count);

        int[] map = new int[size];
        for (int i = 0; i < size; i++) {
            map[entries[i].index] = i;
        }

        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = map[blocks[i]];
        }

        List<BlockState> newPalette = Arrays.stream(entries).map(e -> e.state).toList();
        return new SchematicaOutputData(width, height, length, newPalette, blocks);
    }

    private record SortEntry(BlockState state, int index, int count) {}
}