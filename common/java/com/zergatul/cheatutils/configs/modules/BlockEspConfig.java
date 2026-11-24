package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

public record BlockEspConfig(
        ImmutableList<BlockEspEntry> entries
) implements ImmutableConfig, ModuleStateProvider {

    public BlockEspConfig {
        if (entries == null) {
            entries = ImmutableList.empty();
        }

        entries = entries
                .removeAll(Objects::isNull)
                .replaceAll(new DuplicatedBlocksFilter())
                .removeAll(e -> e.blocks().isEmpty());
    }

    public static BlockEspConfig defaults() {
        return new Builder()
                .entries(ImmutableList.empty())
                .build();
    }

    @Override
    public boolean isEnabled() {
        return entries.stream().anyMatch(BlockEspEntry::enabled);
    }

    // <generated-code>

    public BlockEspConfig withEntries(ImmutableList<BlockEspEntry> entries) {
        return Builder.from(this).entries(entries).build();
    }

    public static class Builder {

        private ImmutableList<BlockEspEntry> entries;

        private Builder() {}

        public static Builder from(BlockEspConfig config) {
            return new Builder()
                    .entries(config.entries);
        }

        public Builder entries(ImmutableList<BlockEspEntry> entries) {
            this.entries = entries;
            return this;
        }

        public BlockEspConfig build() {
            return new BlockEspConfig(entries);
        }
    }

    // </generated-code>

    private static class DuplicatedBlocksFilter implements UnaryOperator<BlockEspEntry> {

        private final Set<Block> visited;

        private DuplicatedBlocksFilter() {
            this.visited = new HashSet<>();
        }

        @Override
        public BlockEspEntry apply(BlockEspEntry entry) {
            ImmutableList<Block> filtered = entry.blocks().removeAll(b -> !visited.add(b));
            if (filtered != entry.blocks()) {
                return entry.withBlocks(filtered);
            } else {
                return entry;
            }
        }
    }
}