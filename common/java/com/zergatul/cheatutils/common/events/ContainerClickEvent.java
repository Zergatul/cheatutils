package com.zergatul.cheatutils.common.events;

import net.minecraft.world.inventory.ContainerInput;

public record ContainerClickEvent(int slot, int button, ContainerInput type) {}