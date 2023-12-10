package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.controllers.TeleportHackController;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.utils.MathUtils;

public class TeleportApi {

    @ApiVisibility(ApiType.ACTION)
    public boolean toCrosshair(double distance, int repeats) {
        distance = MathUtils.clamp(distance, 1, 1000);
        repeats = MathUtils.clamp(repeats, 0, 100);
        return TeleportHackController.instance.teleportToCrosshair(distance, repeats);
    }

    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double distance, int repeats) {
        distance = MathUtils.absClamp(distance, 1, 1000);
        repeats = MathUtils.clamp(repeats, 0, 100);
        return TeleportHackController.instance.verticalTeleport(distance, repeats);
    }

    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double fromDistance, double toDistance, boolean findSurface, int repeats) {
        fromDistance = MathUtils.absClamp(fromDistance, -1000, 1000);
        toDistance = MathUtils.absClamp(toDistance, -1000, 1000);
        repeats = MathUtils.clamp(repeats, 0, 100);
        return TeleportHackController.instance.verticalTeleport(fromDistance, toDistance, findSurface, repeats);
    }
}