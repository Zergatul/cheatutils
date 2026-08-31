package net.caffeinemc.mods.sodium.api.vertex.buffer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import org.lwjgl.system.MemoryStack;

/**
 * Compile-time stub for optional Sodium integration. This interface is excluded from produced jars.
 */
public interface VertexBufferWriter {
    static VertexBufferWriter of(VertexConsumer consumer) {
        return (VertexBufferWriter) consumer;
    }

    static VertexBufferWriter tryOf(VertexConsumer consumer) {
        return consumer instanceof VertexBufferWriter writer ? writer : null;
    }

    static void copyInto(
            VertexBufferWriter writer,
            MemoryStack stack,
            long ptr,
            int count,
            VertexFormatDescription format
    ) {
        writer.push(stack, ptr, count, format);
    }

    void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format);

    default boolean canUseIntrinsics() {
        return true;
    }
}