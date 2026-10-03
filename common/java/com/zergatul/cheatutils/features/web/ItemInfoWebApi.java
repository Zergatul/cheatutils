package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemInfoWebApi extends WebApiBase {

    public static final ItemInfoWebApi INSTANCE = new ItemInfoWebApi();

    private ItemInfoWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "item-info";
    }

    @Override
    public String get() {
        return gson.toJson(RegistryExtensions.getValues(BuiltInRegistries.ITEM));
    }
}