package com.zergatul.cheatutils.controllers;

import com.google.common.collect.ImmutableList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.BrandingControlMixinInterface;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;

public class HardSwitchController {

    public static final HardSwitchController instance = new HardSwitchController();

    private boolean turnedOff;

    private HardSwitchController() {

    }

    public boolean isModValidById(String modId) {
        if ("zergatulcheatutils".equals(modId))
            return false;
        return true;
    }

    public boolean isTurnedOff() {
        return turnedOff;
    }

    public void turnOff() {
        if (!turnedOff) {

            turnedOff = true;

            ImmutableList.Builder<String> brd = ImmutableList.builder();
            brd.add("Forge " + ForgeVersion.getVersion());
            brd.add("Minecraft " + MCPVersion.getMCVersion());
            brd.add("MCP " + MCPVersion.getMCPVersion());
            long tModCount = ModList.get().getMods().stream().filter(m -> isModValidById(m.getModId())).count();
            brd.add(ForgeI18n.parseMessage("fml.menu.loadingmods", tModCount));

            ImmutableList<String> list = brd.build();
            BrandingControlMixinInterface branding = (BrandingControlMixinInterface)new BrandingControl();
            branding.setBrandings(list);
            branding.setBrandingsNoMC(list.subList(1, list.size()));

            ConfigStore.instance.getConfig().esp = false;
            ConfigStore.instance.getConfig().fullBrightConfig.enabled = false;
            ConfigStore.instance.getConfig().autoFishConfig.enabled = false;
            ConfigStore.instance.getConfig().shulkerTooltipConfig.enabled = false;
        }
    }


}