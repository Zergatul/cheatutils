package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.schematics.SchemaFormatFactory;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.Base64;

public class SchematicaPlaceApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-place";
    }

    @Override
    public String post(String body) throws IOException, InvalidFormatException {
        Request request = gson.fromJson(body, Request.class);
        byte[] data = Base64.getDecoder().decode(request.file);
        SchemaFile schema = SchemaFormatFactory.parse(data, request.name);

        // replace palette
        for (PaletteEntry entry : request.palette) {
            if (0 < entry.id && entry.id < schema.getPalette().length) {
                schema.getPalette()[entry.id] = entry.state;
            }
        }

        Schematica.instance.place(schema, request.name, request.placing);
        return "{}";
    }

    public record Request(String file, String name, PlacingSettings placing, PaletteEntry[] palette) {}

    public record PaletteEntry(int id, BlockState state) {}
}