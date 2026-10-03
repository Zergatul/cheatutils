package com.zergatul.cheatutils.modules.automation.schematica.web;

import com.zergatul.cheatutils.modules.automation.schematica.formats.InvalidFormatException;
import com.zergatul.cheatutils.modules.automation.schematica.formats.PaletteEntry;
import com.zergatul.cheatutils.modules.automation.schematica.formats.SchemaFile;
import com.zergatul.cheatutils.modules.automation.schematica.formats.SchemaFormatFactory;
import com.zergatul.cheatutils.web.WebApiBase;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Base64;

public class UploadWebApi extends WebApiBase {

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
        int size = states.length;
        PaletteEntry[] palette = new PaletteEntry[size];
        for (int i = 0; i < size; i++) {
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