package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class NotificationsWebApi extends WebApiBase {

    public static final NotificationsWebApi INSTANCE = new NotificationsWebApi();

    private NotificationsWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "notifications";
    }

    @Override
    public String get() {
        return gson.toJson(ScriptRuntimeFailureHandler.instance.getNotificationHistory());
    }
}