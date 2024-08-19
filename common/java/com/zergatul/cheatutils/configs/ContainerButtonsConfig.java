package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import net.minecraft.world.item.Item;

public class ContainerButtonsConfig implements ModuleStateProvider {

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
}