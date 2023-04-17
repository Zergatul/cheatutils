package com.zergatul.cheatutils.common.events;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GatherTooltipComponentsEvent(ItemStack itemStack, List<Component> list) {}