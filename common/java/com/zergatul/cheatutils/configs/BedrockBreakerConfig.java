package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.world.item.Items;

public class BedrockBreakerConfig {
    public boolean placeSupportBlock;
    public String supportBlockId;
    public boolean replace;
    public String replaceBlockId;

    public BedrockBreakerConfig() {
        supportBlockId = Registries.ITEMS.getKey(Items.NETHERRACK).toString();
        replaceBlockId = Registries.ITEMS.getKey(Items.NETHERRACK).toString();
    }
}