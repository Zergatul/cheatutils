package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.controllers.TeleportHackController;
import com.zergatul.cheatutils.utils.MathUtils;

public class TeleportApi {

    public boolean toCrosshair(double distance, int repeats) {
        distance = MathUtils.clamp(distance, 1, 1000);
        repeats = MathUtils.clamp(repeats, 0, 100);
        return TeleportHackController.instance.teleportToCrosshair(distance, repeats);
    }

    public boolean vertical(double distance, int repeats) {
        distance = MathUtils.clamp(distance, -1000, 1000);
        repeats = MathUtils.clamp(repeats, 0, 100);
        return TeleportHackController.instance.verticalTeleport(distance, repeats);
    }
}