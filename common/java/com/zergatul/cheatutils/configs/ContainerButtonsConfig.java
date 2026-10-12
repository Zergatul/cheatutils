package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import net.minecraft.world.item.Item;

public class ContainerButtonsConfig implements ModuleStateProvider, Sanitizable {

    public boolean showTakeAll;
    public boolean showSmartPut;
    public boolean checkShulkerContent;
    public boolean showDropAll;
    public boolean autoTakeAll;
    public boolean autoDropAll;
    public boolean autoClose;
    public boolean useFilter;
    public ImmutableList<Item> items = new ImmutableList<>();

    @Override
    public boolean isEnabled() {
        return showTakeAll || showSmartPut || showDropAll || autoTakeAll || autoDropAll || autoClose;
    }

    @Override
    public void sanitize() {
        if (autoTakeAll && autoDropAll) {
            // these 2 options can't be used at the same time
            autoTakeAll = false;
            autoDropAll = false;
        }
    }
}