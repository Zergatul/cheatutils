package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import net.minecraft.SharedConstants;

public class GeneralInformationApi extends ApiBase {

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