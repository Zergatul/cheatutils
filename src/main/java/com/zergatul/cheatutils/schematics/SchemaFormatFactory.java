package com.zergatul.cheatutils.schematics;

import java.io.IOException;
import java.util.Locale;

public class SchemaFormatFactory {
    public static SchemaFile parse(byte[] data, String filename) throws IOException, InvalidFormatException {
        filename = filename.toLowerCase(Locale.ROOT);
        String extension = filename.substring(filename.lastIndexOf('.'));
        return switch (extension) {
            case ".schematic" -> new SchematicFile(data);
            case ".litematic" -> new LitematicFile(data);
            default -> throw new InvalidFormatException("Unexpected file extension.");
        };
    }
}