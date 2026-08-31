package com.zergatul.cheatutils.schematics;

import java.util.Locale;

public class SchemaFormatFactory {

    public static SchemaFile parse(byte[] data, String filename) throws InvalidFormatException {
        filename = filename.toLowerCase(Locale.ROOT);
        int index = filename.lastIndexOf('.');
        if (index < 0) {
            throw new InvalidFormatException("Unexpected file extension.");
        }
        String extension = filename.substring(index);
        return switch (extension) {
            case ".schematic" -> SchematicaFile.parse(data);
            case ".schem" -> SpongeSchematicaFile.parse(data);
            case ".litematic" -> LitematicaFile.parse(data);
            default -> throw new InvalidFormatException("Unexpected file extension.");
        };
    }
}