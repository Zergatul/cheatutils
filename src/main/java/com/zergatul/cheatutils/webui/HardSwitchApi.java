package com.zergatul.cheatutils.webui;

public class HardSwitchApi extends ApiBase {

    @Override
    public String getRoute() {
        return "hard-switch";
    }

    /*@Override
    public String post(String body) throws MethodNotSupportedException {
        HardSwitchController.instance.turnOff();
        return "true";
    }*/
}