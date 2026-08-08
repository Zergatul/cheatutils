package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.visuals.FakeWeather;
import org.apache.http.HttpException;

public class FakeWeatherSetTimeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "fake-weather-set-time";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        FakeWeather.instance.setTime(WebHelper.requireField(request.value, "value"));
        return "{ \"ok\": true }";
    }

    public record Request(Integer value) {}
}