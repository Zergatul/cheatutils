package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.controllers.TpsCounterController;

public class TpsApi {

    public double get() {
        return TpsCounterController.instance.getTps();
    }
}