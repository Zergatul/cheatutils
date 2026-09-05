package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;

public class NotificationsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "notifications";
    }

    @Override
    public String get() {
        return gson.toJson(ScriptRuntimeFailureHandler.instance.getNotificationHistory());
    }
}