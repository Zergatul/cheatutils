package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import org.apache.http.HttpException;

public class VillagerRollerStatusApi extends ApiBase {

    @Override
    public String getRoute() {
        return "villager-roller-status";
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = gson.fromJson(body, Request.class);
        if (request.start) {
            VillagerRoller.instance.start();
        }
        if (request.stop) {
            VillagerRoller.instance.stop();
        }
        return "{}";
    }

    public record Request(boolean start, boolean stop) {}
}