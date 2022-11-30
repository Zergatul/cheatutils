package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.EntityUtils;
import org.apache.http.MethodNotSupportedException;

public class EntityInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entity-info";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return gson.toJson(EntityUtils.getEntityClasses());
    }
}