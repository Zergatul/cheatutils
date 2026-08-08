package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import org.apache.http.HttpException;

public class VillagerRollerStatusApi extends ApiBase {

    @Override
    public String getRoute() {
        return "villager-roller-status";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        boolean start = Boolean.TRUE.equals(request.start);
        boolean stop = Boolean.TRUE.equals(request.stop);
        if (start == stop) {
            throw new ApiException("Exactly one of start or stop must be true", HttpResponseCodes.BAD_REQUEST);
        }
        ClientThreadDispatcher.run(() -> {
            if (start) {
                VillagerRoller.instance.start();
            }
            if (stop) {
                VillagerRoller.instance.stop();
            }
        });
        return "{}";
    }

    public record Request(Boolean start, Boolean stop) {}
}