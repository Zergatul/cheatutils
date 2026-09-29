package com.zergatul.cheatutils.collections;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@NullMarked
public class OneOf3<T1, T2, T3> {

    private final @Nullable T1 value1;
    private final @Nullable T2 value2;
    private final @Nullable T3 value3;

    private OneOf3(@Nullable T1 value1, @Nullable T2 value2, @Nullable T3 value3) {
        if (value1 == null && value2 == null && value3 == null) {
            throw new IllegalArgumentException("Values are null.");
        }

        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public static <T1, T2, T3> OneOf3<T1, T2, T3> from1(T1 value) {
        return new OneOf3<>(value, null, null);
    }

    public static <T1, T2, T3> OneOf3<T1, T2, T3> from2(T2 value) {
        return new OneOf3<>(null, value, null);
    }

    public static <T1, T2, T3> OneOf3<T1, T2, T3> from3(T3 value) {
        return new OneOf3<>(null, null, value);
    }

    public <T4> T4 match(Function<T1, T4> map1, Function<T2, T4> map2, Function<T3, T4> map3) {
        if (value1 != null) {
            return map1.apply(value1);
        }
        if (value2 != null) {
            return map2.apply(value2);
        }
        if (value3 != null) {
            return map3.apply(value3);
        }
        throw new IllegalStateException();
    }

    public void accept(Consumer<T1> handle1, Consumer<T2> handle2, Consumer<T3> handle3) {
        if (value1 != null) {
            handle1.accept(value1);
            return;
        }
        if (value2 != null) {
            handle2.accept(value2);
            return;
        }
        if (value3 != null) {
            handle3.accept(value3);
            return;
        }
        throw new IllegalStateException();
    }
}