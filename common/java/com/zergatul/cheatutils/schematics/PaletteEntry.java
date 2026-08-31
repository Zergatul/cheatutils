package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.state.BlockState;

public record PaletteEntry(String raw, BlockState state) {}