package com.zergatul.cheatutils.configs;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class AutoDropConfig implements ModuleStateProvider {

    public List<Item> items = new ArrayList<>();

    @Override
    public boolean isEnabled() {
        return !items.isEmpty();
    }
}