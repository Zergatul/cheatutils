package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.visuals.Zoom;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class ZoomApi {

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