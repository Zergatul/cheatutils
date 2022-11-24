package com.zergatul.cheatutils.webui;

import net.minecraftforge.registries.ForgeRegistries;
import org.apache.http.HttpException;

import java.util.Objects;

public class ItemInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "item-info";
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(ForgeRegistries.ITEMS.getValues().stream().filter(Objects::nonNull).toArray());
    }
}