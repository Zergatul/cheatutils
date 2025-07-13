package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.*;

@CustomType(name = "Tag")
public abstract class TagWrapper {

    public int getIntOr(int defaultValue) {
        return defaultValue;
    }

    public long getLongOr(long defaultValue) {
        return defaultValue;
    }

    public double getFloatOr(double defaultValue) {
        return defaultValue;
    }

    public String getStringOr(String defaultValue) {
        return defaultValue;
    }

    static TagWrapper from(Tag tag) {
        if (tag == null) {
            return MissingTagWrapper.instance;
        }

        return switch (tag.getId()) {
            case Tag.TAG_BYTE -> new ByteTagWrapper((ByteTag) tag);
            case Tag.TAG_SHORT -> new ShortTagWrapper((ShortTag) tag);
            case Tag.TAG_INT -> new IntTagWrapper((IntTag) tag);
            case Tag.TAG_LONG -> new LongTagWrapper((LongTag) tag);
            case Tag.TAG_FLOAT -> new FloatTagWrapper((FloatTag) tag);
            case Tag.TAG_DOUBLE -> new DoubleTagWrapper((DoubleTag) tag);
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTagWrapper((ByteArrayTag) tag);
            case Tag.TAG_STRING -> new StringTagWrapper((StringTag) tag);
            case Tag.TAG_LIST -> new ListTagWrapper((ListTag) tag);
            case Tag.TAG_COMPOUND -> new CompoundTagWrapper((CompoundTag) tag);
            case Tag.TAG_INT_ARRAY -> new IntArrayTagWrapper((IntArrayTag) tag);
            case Tag.TAG_LONG_ARRAY -> new LongArrayTagWrapper((LongArrayTag) tag);
            default -> MissingTagWrapper.instance;
        };
    }
}