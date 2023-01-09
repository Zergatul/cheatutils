package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.SchematicaController;
import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchematicFile;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.Base64;

public class SchematicaPlaceApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-place";
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = gson.fromJson(body, Request.class);
        byte[] data = Base64.getDecoder().decode(request.file);
        SchematicFile schematic;
        try {
            schematic = new SchematicFile(data);
        }
        catch (IOException | InvalidFormatException e) {
            throw new HttpException(e.getMessage());
        }

        SchematicaController.instance.place(schematic, request.placing);
        return "{}";
    }

    @Override
    public String delete(String id) throws HttpException {
        SchematicaController.instance.clear();
        return "{}";
    }

    public record Request(String file, PlacingSettings placing) {}
}