package com.zergatul.cheatutils.modules.esp.block;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NullMarked
public class BlockGroupKey {

    private static final String DELIMITER = ";";

    private final String[] blocks;
    private final int hashCode;

    public BlockGroupKey(List<Block> blocks) {
        this(toBlockIds(blocks));
    }

    public BlockGroupKey(ImmutableList<Block> blocks) {
        this(toBlockIds(blocks));
    }

    private BlockGroupKey(String[] blocks) {
        Arrays.sort(blocks);
        this.blocks = blocks;
        this.hashCode = Arrays.hashCode(this.blocks);
    }

    public static BlockGroupKey parse(String value) {
        String[] parts = value.split(DELIMITER);

        List<Block> blocks = new ArrayList<>(parts.length);
        for (String part : parts) {
            Block block = RegistryExtensions.safeParse(BuiltInRegistries.BLOCK, part);
            if (block == null) {
                throw new IllegalArgumentException("Invalid block ID: " + part);
            }
            blocks.add(block);
        }

        return new BlockGroupKey(blocks);
    }

    public @Nullable BlockEspEntry findIn(ImmutableList<BlockEspEntry> entries) {
        for (BlockEspEntry entry : entries) {
            if (new BlockGroupKey(entry.blocks()).equals(this)) {
                return entry;
            }
        }
        return null;
    }

    public boolean isValid() {
        if (this.blocks.length == 0) {
            return false;
        }

        Block[] blocks = new Block[this.blocks.length];
        for (int i = 0; i < this.blocks.length; i++) {
            Block block = RegistryExtensions.safeParse(BuiltInRegistries.BLOCK, this.blocks[i]);
            if (block == null) {
                return false;
            }
            blocks[i] = block;
        }

        BlockEspEntry entry = BlockEspEntry.defaults().withBlocks(ImmutableList.from(blocks));
        return entry.blocks().size() == this.blocks.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BlockGroupKey other) {
            if (other.hashCode != this.hashCode) {
                return false;
            }
            return Arrays.equals(this.blocks, other.blocks);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return String.join(DELIMITER, this.blocks);
    }

    private static String[] toBlockIds(List<Block> blocks) {
        String[] array = new String[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            array[i] = BuiltInRegistries.BLOCK.getKey(blocks.get(i)).toString();
        }
        return array;
    }

    private static String[] toBlockIds(ImmutableList<Block> blocks) {
        String[] array = new String[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            array[i] = BuiltInRegistries.BLOCK.getKey(blocks.get(i)).toString();
        }
        return array;
    }
}