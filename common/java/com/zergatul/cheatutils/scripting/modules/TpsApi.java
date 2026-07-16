package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.controllers.TpsCounterController;
import com.zergatul.scripting.MethodDescription;

public class TpsApi {

    @MethodDescription("Estimates server TPS from the last 20 time updates. Returns 0 until enough samples arrive.")
    public double get() {
        return TpsCounterController.instance.getTps();
    }
}