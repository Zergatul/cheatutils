package com.zergatul.cheatutils.common.events;

import net.minecraft.world.inventory.ClickType;

public record ContainerClickEvent(int slot, int button, ClickType type) {}