package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureResources;

public class Segment {
    public final SegmentPos pos;
    public final NativeImage image;
    public final TextureResources texture;
    public boolean updated;
    public long updateTime;

    public Segment(SegmentPos pos, int segmentSize) {
        this.pos = pos;
        this.image = new NativeImage(segmentSize * 16, segmentSize * 16, true);
        this.texture = TextureResources.from2dImage(() -> "Chunk overlay segment", image);
    }

    public void onChange() {
        image.writeToGpuTexture(RenderSystem.getDevice().createCommandEncoder(), texture.texture());
    }

    public void close() {
        texture.close();
        image.close();
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Segment segment)) {
            return false;
        } else {
            return this.pos.equals(segment.pos);
        }
    }
}