package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import org.apache.http.HttpException;

import java.util.Objects;

public class ItemInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "item-info";
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(Registries.ITEMS.getValues().stream().filter(Objects::nonNull).toArray());
    }
}