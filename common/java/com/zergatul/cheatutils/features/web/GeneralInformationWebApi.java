package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.SharedConstants;

public class GeneralInformationWebApi extends WebApiBase {

    public static final GeneralInformationWebApi INSTANCE = new GeneralInformationWebApi();

    private GeneralInformationWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "general-information";
    }

    @Override
    public String get() throws Throwable {
        LoaderEnvironment environment = LoaderBridge.INSTANCE.getEnvironment();
        String gameVersion = "Minecraft: " + SharedConstants.getCurrentVersion().name();
        String modLoaderVersion = environment.getLoaderName() + ": " + environment.getLoaderVersion();
        String modVersion = Constants.MOD_ID + ": " + environment.getModVersion();
        String modCount = "Mods: " + environment.getModCount();
        Response response = new Response(gameVersion, modLoaderVersion, modVersion, modCount);
        return gson.toJson(response);
    }

    public record Response(String gameVersion, String modLoaderVersion, String modVersion, String modCount) {}
}