package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.Blocks;

import java.io.FileOutputStream;

public class test {

    public static void rofl() {
        var schematica = new SchematicFile(10, 10, 10);
        schematica.setPaletteEntry(49, Blocks.OBSIDIAN);
        for (int i = 0; i < 10; i++) {
            schematica.setBlock(i, i, i, Blocks.OBSIDIAN);
        }
        try {
            FileOutputStream output = new FileOutputStream("C:\\Users\\Zergatul\\Documents\\Schematica\\Sier.schematica");
            schematica.write(output);
            output.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
