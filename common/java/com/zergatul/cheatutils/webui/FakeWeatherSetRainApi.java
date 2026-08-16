package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.visuals.FakeWeather;
import org.apache.http.HttpException;

public class FakeWeatherSetRainApi extends ApiBase {

    @Override
    public String getRoute() {
        return "fake-weather-set-rain";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        float value = WebHelper.requireField(request.value, "value");
        WebHelper.requireFinite(value, "value");
        FakeWeather.instance.setRain(value);
        return "{ \"ok\": true }";
    }

    public record Request(Float value) {}
}