package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.schematics.InvalidFormatException;
import com.zergatul.cheatutils.schematics.SchematicFile;
import net.minecraft.world.level.block.Block;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.Base64;

public class SchematicaUploadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-upload";
    }

    @Override
    public String post(String body) throws HttpException {
        byte[] data = Base64.getDecoder().decode(body);
        SchematicFile schematic;
        try {
            schematic = new SchematicFile(data);
        }
        catch (IOException | InvalidFormatException e) {
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }

        return gson.toJson(new SuccessResponse(
                schematic.getSummary(),
                schematic.getPalette(),
                schematic.getWidth(),
                schematic.getHeight(),
                schematic.getLength()));
    }

    public record ErrorResponse(String error) {}

    public record SuccessResponse(int[] summary, Block[] palette, int width, int height, int length) {}
}