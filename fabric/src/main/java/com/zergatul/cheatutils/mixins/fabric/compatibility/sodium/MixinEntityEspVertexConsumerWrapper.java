package com.zergatul.cheatutils.mixins.fabric.compatibility.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.utils.UnsafeUtil;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
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

    @Shadow
    private FloatList overlayList;

    @Shadow
    private FloatList outlineList;

    public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        int length = count * format.stride();
        long copy = stack.nmalloc(length);
        UNSAFE.copyMemory(ptr, copy, length);

        if (this.consumer instanceof VertexBufferWriter writer) {
            writer.push(stack, copy, count, format);
        }

        int positionOffset = format.getElementOffset(CommonVertexAttribute.POSITION);
        int uvOffset = format.getElementOffset(CommonVertexAttribute.TEXTURE);
        int stride = format.stride();
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