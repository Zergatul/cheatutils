package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.visuals.Zoom;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

public class ZoomApi {

    @MethodDescription("Starts a smooth zoom. FOV outside [0.01, 150] or duration above 10 seconds is ignored.")
    @ApiVisibility(ApiType.UPDATE)
    public void start(double fov, double seconds) {
        if (fov < 0.01 || fov > 150) {
            return;
        }
        if (seconds > 10) {
            return;
        }
        Zoom.instance.startZooming(fov, seconds);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void stop() {
        Zoom.instance.stopZooming();
    }
}