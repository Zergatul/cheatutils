package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

public class UserWebApi extends WebApiBase {

    public static final UserWebApi INSTANCE = new UserWebApi();

    private UserWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "user";
    }

    @Override
    public String get() {
        return Minecraft.getInstance().getUser().getName();
    }
}