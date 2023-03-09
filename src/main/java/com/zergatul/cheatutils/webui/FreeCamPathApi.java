package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.FreeCamController;
import org.apache.http.HttpException;

public class FreeCamPathApi extends ApiBase {

    @Override
    public String getRoute() {
        return "free-cam-path";
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(FreeCamController.instance.getPath().get());
    }

    @Override
    public String post(String body) throws HttpException {
        Double time = gson.fromJson(body, Double.class);
        if (time == null) {
            return "{}";
        }

        FreeCamController.instance.getPath().add(time);
        return "{ \"ok\": true }";
    }

    @Override
    public String delete(String id) throws HttpException {
        FreeCamController.instance.getPath().clear();
        return "{ \"ok\": true }";
    }
}
