package com.zergatul.cheatutils.schematics;

import java.util.Locale;

public class SchemaFormatFactory {

    public static SchemaFile parse(byte[] data, String filename) throws InvalidFormatException {
        filename = filename.toLowerCase(Locale.ROOT);
        String extension = filename.substring(filename.lastIndexOf('.'));
        return switch (extension) {
            case ".schematic" -> SchematicaFile.parse(data);
            case ".schem" -> SpongeSchematicaFile.parse(data);
            case ".litematic" -> LitematicaFile.parse(data);
            default -> throw new InvalidFormatException("Unexpected file extension.");
        };
    }
}