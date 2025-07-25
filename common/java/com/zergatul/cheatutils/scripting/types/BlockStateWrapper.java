package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("unused")
@CustomType(name = "BlockState")
public class BlockStateWrapper {

    private final BlockState state;

    public BlockStateWrapper(BlockState state) {
        this.state = state;
    }

    @Getter(name = "block")
    public BlockWrapper getBlock() {
        return new BlockWrapper(state.getBlock());
    }

    public boolean canBeReplaced() {
        return state.canBeReplaced();
    }

    public boolean isFluidSource() {
        return state.getFluidState().isSource();
    }

    public boolean hasTag(String tag) {
        return state.getValues().keySet().stream().anyMatch(p -> p.getName().equals(tag));
    }

    public boolean getBooleanTag(BlockPosWrapper pos, String tag) {
        return state.getValues().keySet().stream()
                .filter(p -> p.getName().equals(tag) && p instanceof BooleanProperty)
                .findFirst()
                .map(p -> (Boolean) state.getValue(p))
                .orElse(false);
    }

    public int getIntegerTag(BlockPosWrapper pos, String tag) {
        return state.getValues().keySet().stream()
                .filter(p -> p.getName().equals(tag) && p instanceof IntegerProperty)
                .findFirst()
                .map(p -> (Integer) state.getValue(p))
                .orElse(Integer.MIN_VALUE);
    }

    public String getEnumTag(BlockPosWrapper pos, String tag) {
        return state.getValues().keySet().stream()
                .filter(p -> p.getName().equals(tag) && p instanceof EnumProperty)
                .findFirst()
                .map(p -> state.getValue(p).toString())
                .orElse("");
    }
}