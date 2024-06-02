package com.zergatul.cheatutils.chunkoverlays;

import net.minecraft.world.level.ChunkPos;

public class SegmentPos {
    public int x;
    public int z;

    public SegmentPos(ChunkPos pos, int segmentSize) {
        this.x = Math.floorDiv(pos.x, segmentSize);
        this.z = Math.floorDiv(pos.z, segmentSize);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SegmentPos pos)) {
            return false;
        } else {
            return this.x == pos.x && this.z == pos.z;
        }
    }
}
