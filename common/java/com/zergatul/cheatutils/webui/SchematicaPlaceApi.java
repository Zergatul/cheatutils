package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.schematics.SchemaFormatFactory;
import org.apache.http.HttpException;

import java.io.IOException;

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
        WebHelper.requireField(request.placing, "placing");
        SchemaFile schema;
        try {
            schema = SchemaFormatFactory.parse(data, request.name);
        }
        catch (IOException | InvalidFormatException e) {
            throw new HttpException(e.getMessage());
        }

        ClientThreadDispatcher.run(() -> Schematica.instance.place(schema, request.placing));
        return "{}";
    }

    @Override
    public String delete(String id) throws HttpException {
        ClientThreadDispatcher.run(Schematica.instance::clear);
        return "{}";
    }

    public record Request(String file, String name, PlacingSettings placing) {}
}