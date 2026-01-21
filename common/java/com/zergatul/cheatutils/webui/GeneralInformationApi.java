package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.wrappers.ModEnvironment;
import net.minecraft.SharedConstants;

public class GeneralInformationApi extends ApiBase {

    @Override
    public String getRoute() {
        return "general-information";
    }

    @Override
    public String get() throws Throwable {
        String gameVersion = "Minecraft: " + SharedConstants.getCurrentVersion().name();
        String modLoaderVersion = ModEnvironment.getModLoader() + ": " + ModEnvironment.getModLoaderVersion();
        String modVersion = ModMain.MODID + ": " + ModEnvironment.getModVersion();
        String modCount = "Mods: " + ModEnvironment.getModCount();
        Response response = new Response(gameVersion, modLoaderVersion, modVersion, modCount);
        return gson.toJson(response);
    }

    public record Response(String gameVersion, String modLoaderVersion, String modVersion, String modCount) {}
}