package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.font.SystemFonts;

public class FontsApi extends ApiBase {

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
