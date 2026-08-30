package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.PaletteEntry;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.schematics.SchemaFormatFactory;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Base64;

public class SchematicaUploadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-upload";
    }

    @Override
    public String post(String body) {
        Request request = gson.fromJson(body, Request.class);
        byte[] data = Base64.getDecoder().decode(request.file);
        SchemaFile schema;
        try {
            schema = SchemaFormatFactory.parse(data, request.name);
        } catch (InvalidFormatException e) {
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }

        BlockState[] states = schema.getPalette();
        String[] raw = schema.getRawPalette();
        PaletteEntry[] palette = new PaletteEntry[states.length];
        for (int i = 0; i < states.length; i++) {
            palette[i] = new PaletteEntry(raw[i], states[i]);
        }

        return gson.toJson(new SuccessResponse(
                schema.getSummary(),
                palette,
                schema.getWidth(),
                schema.getHeight(),
                schema.getLength()));
    }

    public record Request(String file, String name) {}
    public record ErrorResponse(String error) {}
    public record SuccessResponse(int[] summary, PaletteEntry[] palette, int width, int height, int length) {}
}