package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;

public class SchematicaSummaryApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-summary";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String get() {
        return gson.toJson(Schematica.instance.getSummary());
    }

    @Override
    public String post(String body) throws ApiException {
        PostRequest request = WebHelper.parseJson(gson, body, PostRequest.class);
        if ("rescan".equals(request.action)) {
            Schematica.instance.rescan(request.index);
        } else if ("move".equals(request.action)) {
            Schematica.instance.move(request.index, request.x, request.y, request.z);
        } else {
            throw new ApiException("Unsupported Schematica action: " + request.action, HttpResponseCodes.BAD_REQUEST);
        }
        return "{}";
    }

    @Override
    public String delete(String id) throws ApiException {
        if (id.equals("all")) {
            Schematica.instance.clear();
        } else {
            try {
                Schematica.instance.remove(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid Schematica index", HttpResponseCodes.BAD_REQUEST, e);
            }
        }
        return "{}";
    }

    public record PostRequest(String action, int index, int x, int y, int z) {}
}