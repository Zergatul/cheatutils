package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.controllers.ZoomController;

public class ZoomApi {

    public void start(double fov, double seconds) {
        if (fov < 0.01 || fov > 150) {
            return;
        }
        if (seconds > 10) {
            return;
        }
        ZoomController.instance.startZooming(fov, seconds);
    }

    public void stop() {
        ZoomController.instance.stopZooming();
    }
}