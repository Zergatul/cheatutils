package com.zergatul.cheatutils.webui;

import net.minecraft.client.Minecraft;
import org.apache.http.MethodNotSupportedException;

public class UserApi extends ApiBase {

    @Override
    public String getRoute() {
        return "user";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return Minecraft.getInstance().getUser().getName();
    }
}
