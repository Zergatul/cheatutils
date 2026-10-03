package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.cheatutils.web.WebApiBase;

public class EntityInfoWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "entity-info";
    }

    @Override
    public String get() {
        return gson.toJson(EntityUtils.getEntityClasses());
    }
}