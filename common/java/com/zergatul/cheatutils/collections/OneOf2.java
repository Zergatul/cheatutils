package com.zergatul.cheatutils.collections;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public class OneOf2<T1, T2> {

    private final @Nullable T1 value1;
    private final @Nullable T2 value2;

    private OneOf2(@Nullable T1 value1, @Nullable T2 value2) {
        if (value1 == null && value2 == null) {
            throw new IllegalArgumentException("Both values are null");
        }

        this.value1 = value1;
        this.value2 = value2;
    }

    public static <T1, T2> OneOf2<T1, T2> from1(T1 value) {
        return new OneOf2<T1, T2>(value, null);
    }

    public static <T1, T2> OneOf2<T1, T2> from2(T2 value) {
        return new OneOf2<T1, T2>(null, value);
    }

    public <T3> T3 match(Function<T1, T3> map1, Function<T2, T3> map2) {
        if (value1 != null) {
            return map1.apply(value1);
        }
        if (value2 != null) {
            return map2.apply(value2);
        }
        throw new IllegalStateException();
    }
}