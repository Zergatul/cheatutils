package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import com.zergatul.cheatutils.controllers.ContainerButtonsController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class ContainerButtonsApi {

    public boolean isAutoTakeAllEnabled() {
        return getConfig().autoTakeAll;
    }

    public boolean isAutoDropAllEnabled() {
        return getConfig().autoDropAll;
    }

    public boolean isAutoCloseEnabled() {
        return getConfig().autoClose;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoTakeAll() {
        ContainerButtonsConfig config = getConfig();
        config.autoTakeAll = !config.autoTakeAll;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoDropAll() {
        ContainerButtonsConfig config = getConfig();
        config.autoDropAll = !config.autoDropAll;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoClose() {
        ContainerButtonsConfig config = getConfig();
        config.autoClose = !config.autoClose;
        ConfigStore.instance.requestWrite();
    }

    /*@ApiVisibility(ApiType.ACTION)
    public void dropAll(boolean autoClose) {
        ContainerButtonsController.instance.dropAll(autoClose);
    }

    @ApiVisibility(ApiType.ACTION)
    public void takeAll(boolean autoClose) {
        ContainerButtonsController.instance.takeAll(autoClose);
    }

    @ApiVisibility(ApiType.ACTION)
    public void smartPut() {
        ContainerButtonsController.instance.smartPut();
    }*/

    private ContainerButtonsConfig getConfig() {
        return ConfigStore.instance.getConfig().containerButtonsConfig;
    }
}