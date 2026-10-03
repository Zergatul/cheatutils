package com.zergatul.cheatutils.modules.automation.schematica.formats;

import net.minecraft.world.level.block.state.BlockState;

public record PaletteEntry(String raw, BlockState state) {}