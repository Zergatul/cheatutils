package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

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

    @MethodDescription("Checks whether this block state has a property with the specified name.")
    public boolean hasTag(String tag) {
        return state.getValues().anyMatch(e -> e.property().getName().equals(tag));
    }

    @MethodDescription("Returns a boolean property, or false if absent or not boolean. pos is ignored.")
    public boolean getBooleanTag(BlockPosWrapper pos, String tag) {
        return state.getValues()
                .map(Property.Value::property)
                .filter(p -> p.getName().equals(tag) && p instanceof BooleanProperty)
                .findFirst()
                .map(p -> (Boolean) state.getValue(p))
                .orElse(false);
    }

    @MethodDescription("Returns an integer property, or Integer.MIN_VALUE if absent or not integer. pos is ignored.")
    public int getIntegerTag(BlockPosWrapper pos, String tag) {
        return state.getValues()
                .map(Property.Value::property)
                .filter(p -> p.getName().equals(tag) && p instanceof IntegerProperty)
                .findFirst()
                .map(p -> (Integer) state.getValue(p))
                .orElse(Integer.MIN_VALUE);
    }

    @MethodDescription("Returns an enum property, or an empty string if absent or not enum. pos is ignored.")
    public String getEnumTag(BlockPosWrapper pos, String tag) {
        return state.getValues()
                .map(Property.Value::property)
                .filter(p -> p.getName().equals(tag) && p instanceof EnumProperty<?>)
                .findFirst()
                .map(p -> state.getValue(p).toString())
                .orElse("");
    }
}