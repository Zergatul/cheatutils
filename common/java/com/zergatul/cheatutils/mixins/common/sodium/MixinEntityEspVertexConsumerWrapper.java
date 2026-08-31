package com.zergatul.cheatutils.mixins.common.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.collections.FloatList;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.zergatul.cheatutils.modules.esp.EntityEsp$EntityMaskVertexConsumer", remap = false)
public abstract class MixinEntityEspVertexConsumerWrapper implements VertexBufferWriter {

    @Unique
    private static final int[] TRIANGLE_INDICES_CU = { 0, 1, 3, 1, 2, 3 };

    @Shadow(remap = false)
    private VertexConsumer consumer;

    @Shadow(remap = false)
    private FloatList overlay;

    @Shadow(remap = false)
    private FloatList outline;

    @Override
    public boolean canUseIntrinsics() {
        return VertexBufferWriter.tryOf(this.consumer) != null;
    }

    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        VertexBufferWriter.copyInto(VertexBufferWriter.of(this.consumer), stack, ptr, count, format);

        int positionOffset = format.getElementOffset(CommonVertexAttribute.POSITION);
        int uvOffset = format.getElementOffset(CommonVertexAttribute.TEXTURE);
        int stride = format.stride();
        float[] quad = new float[20];
        int quadVertices = 0;
        for (int i = 0; i < count; i++) {
            long offset = ptr + (long) i * stride;
            int index = quadVertices * 5;
            quad[index] = MemoryUtil.memGetFloat(offset + positionOffset);
            quad[index + 1] = MemoryUtil.memGetFloat(offset + positionOffset + 4);
            quad[index + 2] = MemoryUtil.memGetFloat(offset + positionOffset + 8);
            quad[index + 3] = MemoryUtil.memGetFloat(offset + uvOffset);
            quad[index + 4] = MemoryUtil.memGetFloat(offset + uvOffset + 4);
            quadVertices++;
            if (quadVertices == 4) {
                appendQuad_CU(this.overlay, quad);
                appendQuad_CU(this.outline, quad);
                quadVertices = 0;
            }
        }
    }

    @Unique
    private static void appendQuad_CU(FloatList target, float[] quad) {
        if (target == null) {
            return;
        }
        for (int vertex : TRIANGLE_INDICES_CU) {
            int index = vertex * 5;
            target.add(quad[index]);
            target.add(quad[index + 1]);
            target.add(quad[index + 2]);
            target.add(quad[index + 3]);
            target.add(quad[index + 4]);
        }
    }
}