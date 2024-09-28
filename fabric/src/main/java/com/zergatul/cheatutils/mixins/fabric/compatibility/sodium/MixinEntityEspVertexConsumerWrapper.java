package com.zergatul.cheatutils.mixins.fabric.compatibility.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.utils.UnsafeUtil;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import sun.misc.Unsafe;

@Mixin(value = EntityEsp.VertexConsumerWrapper.class, remap = false)
public abstract class MixinEntityEspVertexConsumerWrapper implements VertexBufferWriter {

    @Unique
    private static final Unsafe UNSAFE = UnsafeUtil.get();

    @Shadow(remap = false)
    private VertexConsumer consumer;

    @Shadow(remap = false)
    private FloatList overlayList;

    @Shadow(remap = false)
    private FloatList outlineList;

    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int length = count * format.getVertexSize();
        long copy = stack.nmalloc(length);
        UNSAFE.copyMemory(ptr, copy, length);

        if (this.consumer instanceof VertexBufferWriter writer) {
            writer.push(stack, copy, count, format);
        }

        int positionOffset = format.getOffset(VertexFormatElement.POSITION);
        int uvOffset = format.getOffset(VertexFormatElement.UV0);
        int stride = format.getVertexSize();
        for (int i = 0; i < count; i++) {
            long offset = ptr + i * stride;
            float x = UNSAFE.getFloat(offset + positionOffset);
            float y = UNSAFE.getFloat(offset + positionOffset + 4);
            float z = UNSAFE.getFloat(offset + positionOffset + 8);
            float u = UNSAFE.getFloat(offset + uvOffset);
            float v = UNSAFE.getFloat(offset + uvOffset + 4);
            if (overlayList != null) {
                overlayList.add(x);
                overlayList.add(y);
                overlayList.add(z);
                overlayList.add(u);
                overlayList.add(v);
            }
            if (outlineList != null) {
                outlineList.add(x);
                outlineList.add(y);
                outlineList.add(z);
                outlineList.add(u);
                outlineList.add(v);
            }
        }
    }
}