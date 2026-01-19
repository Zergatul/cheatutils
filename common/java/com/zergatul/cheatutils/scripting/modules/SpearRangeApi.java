package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.SpearRangeConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.automation.SpearRange;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;

public class SpearRangeApi extends ModuleApi<SpearRangeConfig> {


    @Override
    protected SpearRangeConfig getConfig() {
        return ConfigStore.instance.getConfig().spearRangeConfig;
    }

    @MethodDescription("""
            Returns the position of FIRST spear in the hotbar.
            If no spear is present, returns -1
            """)
    @ApiVisibility(ApiType.ACTION)
    public int getSpearPosition(){
        return SpearRange.instance.spearPos(Minecraft.getInstance().player.getInventory());
    }
}