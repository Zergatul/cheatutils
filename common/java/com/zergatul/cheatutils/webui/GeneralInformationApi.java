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
    public String get() {
        ModLoaderBridge bridge = ModLoaderBridgeInstance.get();
        return gson.toJson(new Response(
                "Minecraft: " + SharedConstants.getCurrentVersion().getName(),
                bridge.getModLoaderName() + ": " + bridge.getModLoaderVersion(),
                Constants.MOD_NAME + ": " + bridge.getModVersion(),
                "Mods: " + bridge.getModCount()));
    }

    public record Response(String gameVersion, String modLoaderVersion, String modVersion, String modCount) {}
}