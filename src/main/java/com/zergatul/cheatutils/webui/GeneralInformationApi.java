package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.ModLoaderInfo;

public class GeneralInformationApi extends ApiBase {

    @Override
    public String getRoute() {
        return "general-information";
    }

    @Override
    public String get() throws Throwable {
        ModLoaderInfo info = ModLoaderInfo.INSTANCE;
        String gameVersion = "Minecraft: " + info.getMinecraftVersion();
        String modLoaderVersion = info.getModLoaderName() + ": " + info.getModLoaderVersion();
        String modVersion = Constants.MOD_ID + ": " + info.getModVersion();
        String modCount = "Mods: " + info.getModCount();
        Response response = new Response(gameVersion, modLoaderVersion, modVersion, modCount);
        return gson.toJson(response);
    }

    public static final class Response {

        public final String gameVersion;
        public final String modLoaderVersion;
        public final String modVersion;
        public final String modCount;

        public Response(String gameVersion, String modLoaderVersion, String modVersion, String modCount) {
            this.gameVersion = gameVersion;
            this.modLoaderVersion = modLoaderVersion;
            this.modVersion = modVersion;
            this.modCount = modCount;
        }
    }
}