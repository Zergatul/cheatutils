package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FontsWebApi extends WebApiBase {

    public static final FontsWebApi INSTANCE = new FontsWebApi();

    private FontsWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "fonts";
    }

    @Override
    public String get() throws Throwable {
        return gson.toJson(SystemFonts.getFontsBlocking()
                .stream()
                .map(i -> new FontInfo(i.getName(), i.getInfo()))
                .toList());
    }

    public record FontInfo(String name, String description) {}
}