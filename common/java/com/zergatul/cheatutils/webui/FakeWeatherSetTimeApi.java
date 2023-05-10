package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.visuals.FakeWeather;
import org.apache.http.HttpException;

public class FakeWeatherSetTimeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "fake-weather-set-time";
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = gson.fromJson(body, Request.class);
        FakeWeather.instance.setTime(request.value);
        return "{ \"ok\": true }";
    }

    public class Request {
        public int value;
    }
}