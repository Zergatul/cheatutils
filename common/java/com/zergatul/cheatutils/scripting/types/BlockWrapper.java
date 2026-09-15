package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("unused")
@CustomType(name = "Block")
public class BlockWrapper {

    private final Block block;

    public BlockWrapper(Block block) {
        this.block = block;
    }

    @Getter(name = "id")
    public String getId() {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    @Getter(name = "tags")
    public String[] getTags() {
        return block.builtInRegistryHolder().tags()
                .map(t -> t.location().toString())
                .toArray(String[]::new);
    }

    public boolean hasTag(String tag) {
        return block.builtInRegistryHolder().tags()
                .anyMatch(t -> t.location().toString().equals(tag));
    }
}