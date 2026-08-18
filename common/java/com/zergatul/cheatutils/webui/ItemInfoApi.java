package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.RegistryExtensions;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "item-info";
    }

    @Override
    public String get() {
        return gson.toJson(RegistryExtensions.getValues(BuiltInRegistries.ITEM));
    }
}