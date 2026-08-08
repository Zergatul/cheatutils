package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.schematics.SchemaFormatFactory;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.http.HttpException;

import java.io.IOException;

public class SchematicaUploadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-upload";
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
        SchemaFile schema;
        try {
            schema = SchemaFormatFactory.parse(data, request.name);
        }
        catch (IOException | InvalidFormatException e) {
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }

        return gson.toJson(new SuccessResponse(
                schema.getSummary(),
                schema.getPalette(),
                schema.getWidth(),
                schema.getHeight(),
                schema.getLength()));
    }

    public record Request(String file, String name) {}

    public record ErrorResponse(String error) {}

    public record SuccessResponse(int[] summary, BlockState[] palette, int width, int height, int length) {}
}