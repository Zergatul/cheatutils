package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.utils.RayCastResult;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.phys.Vec3;

@CustomType(name = "RayCastEntityResult")
public class RayCastEntityResult {

    private final int entityId;
    private final Vec3 location;

    public RayCastEntityResult(RayCastResult result) {
        if (result != null) {
            this.entityId = result.entity().getId();
            this.location = result.hit();
        } else {
            this.entityId = Integer.MIN_VALUE;
            this.location = Vec3.ZERO;
        }
    }

    @Getter(name = "isMiss")
    public boolean getIsMiss() {
        return entityId < 0;
    }

    @Getter(name = "entityId")
    public int getEntityId() {
        return entityId;
    }

    @Getter(name = "location")
    public Position3d getLocation() {
        return new Position3d(location);
    }
}