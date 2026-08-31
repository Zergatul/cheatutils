package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.schematics.SchemaFormatFactory;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.http.HttpException;

public class SchematicaPlaceApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-place";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        byte[] data = WebHelper.decodeBase64(request.file, "file");
        WebHelper.requireNonBlankField(request.name, "name");
        PlacingSettings placing = WebHelper.requireField(request.placing, "placing");
        if (placing.transforms == null) {
            placing.transforms = new String[0];
        }
        SchemaFile schema;
        try {
            schema = SchemaFormatFactory.parse(data, request.name);
        } catch (InvalidFormatException e) {
            throw new HttpException(e.getMessage());
        }

        for (PaletteEntry entry : request.palette == null ? new PaletteEntry[0] : request.palette) {
            if (entry == null || entry.state == null) {
                continue;
            }
            if (0 < entry.id && entry.id < schema.getPalette().length) {
                schema.getPalette()[entry.id] = entry.state;
            }
        }

        Schematica.instance.place(schema, request.name, placing);
        return "{}";
    }

    public record Request(String file, String name, PlacingSettings placing, PaletteEntry[] palette) {}
    public record PaletteEntry(int id, BlockState state) {}
}