package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.HardSwitchController;
import org.apache.http.MethodNotSupportedException;

public class HardSwitchApi extends ApiBase {

    @Override
    public String getRoute() {
        return "hard-switch";
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        HardSwitchController.instance.turnOff();
        return "true";
    }
}
