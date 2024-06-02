package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class Segment {
    public final SegmentPos pos;
    public final NativeImage image;
    public final DynamicTexture texture;
    public boolean updated;
    public long updateTime;

    public Segment(SegmentPos pos, int segmentSize) {
        this.pos = pos;
        this.image = new NativeImage(segmentSize * 16, segmentSize * 16, true);
        this.texture = new DynamicTexture(image);
    }

    public void onChange() {
        texture.upload();
    }

    public void close() {
        texture.close();
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
