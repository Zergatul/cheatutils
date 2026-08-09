package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import org.apache.http.HttpException;

public class FreeCamPathApi extends ApiBase {

    @Override
    public String getRoute() {
        return "free-cam-path";
    }

    @Override
    public String get() throws HttpException {
        return ClientThreadDispatcher.call(() -> gson.toJson(FreeCam.instance.getPath().get()));
    }

    @Override
    public String post(String body) throws HttpException {
        Double time = gson.fromJson(body, Double.class);
        if (time == null) {
            return "{}";
        }

        ClientThreadDispatcher.run(() -> FreeCam.instance.getPath().add(time));
        return "{ \"ok\": true }";
    }

    @Override
    public String delete(String id) throws HttpException {
        ClientThreadDispatcher.run(() -> FreeCam.instance.getPath().clear());
        return "{ \"ok\": true }";
    }
}