package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ContainerButtonsConfig;
import com.zergatul.cheatutils.modules.automation.ContainerButtons;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.scripting.MethodDescription;

import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class ContainerButtonsApi {

    public boolean isAutoTakeAllEnabled() {
        return getConfig().autoTakeAll();
    }

    public boolean isAutoDropAllEnabled() {
        return getConfig().autoDropAll();
    }

    public boolean isAutoCloseEnabled() {
        return getConfig().autoClose();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoTakeAll() {
        update(c -> c.withAutoTakeAll(!c.autoTakeAll()));
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoDropAll() {
        update(c -> c.withAutoDropAll(!c.autoDropAll()));
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleAutoClose() {
        update(c -> c.withAutoClose(!c.autoClose()));
    }

    @ApiVisibility(ApiType.ACTION)
    public void dropAll(boolean autoClose) {
        ContainerButtons.INSTANCE.dropAll(autoClose);
    }

    @ApiVisibility(ApiType.ACTION)
    public void takeAll(boolean autoClose) {
        ContainerButtons.INSTANCE.takeAll(autoClose);
    }

    @MethodDescription("Moves inventory items into the container only when the container already has the same item type.")
    @ApiVisibility(ApiType.ACTION)
    public void smartPut() {
        ContainerButtons.INSTANCE.smartPut();
    }

    private void update(UnaryOperator<ContainerButtonsConfig> update) {
        ContainerButtons.INSTANCE.getSettings().update(update);
    }

    private ContainerButtonsConfig getConfig() {
        return ContainerButtons.INSTANCE.getSettings().get();
    }
}