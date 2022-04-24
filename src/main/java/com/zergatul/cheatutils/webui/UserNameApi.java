package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.SessionNameController;
import net.minecraft.client.Minecraft;
import org.apache.http.MethodNotSupportedException;

public class UserNameApi extends ApiBase {

    @Override
    public String getRoute() {
        return "username";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return Minecraft.getInstance().getUser().getName();
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        SessionNameController.instance.name = body;
        return body;
    }
}
