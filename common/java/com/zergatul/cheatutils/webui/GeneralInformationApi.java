package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import net.minecraft.SharedConstants;

public class GeneralInformationApi extends ApiBase {

    @Override
    public String getRoute() {
        return "general-information";
    }

    @Override
    public String get() throws Throwable {
        ModLoaderBridge bridge = ModLoaderBridgeInstance.get();
        String gameVersion = "Minecraft: " + SharedConstants.getCurrentVersion().name();
        String modLoaderVersion = bridge.getModLoaderName() + ": " + bridge.getModLoaderVersion();
        String modVersion = Constants.MOD_ID + ": " + bridge.getModVersion();
        String modCount = "Mods: " + bridge.getModCount();
        Response response = new Response(gameVersion, modLoaderVersion, modVersion, modCount);
        return gson.toJson(response);
    }

    public record Response(String gameVersion, String modLoaderVersion, String modVersion, String modCount) {}
}