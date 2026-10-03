package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class ResetConfigWebApi extends WebApiBase {

    public static final ResetConfigWebApi INSTANCE = new ResetConfigWebApi();

    private ResetConfigWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "reset-config";
    }

    @Override
    public String post(String body) throws Throwable {
        String[] errors = Profiles.instance.reset();
        return gson.toJson(errors);
    }
}