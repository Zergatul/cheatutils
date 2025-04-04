package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;

public class SchematicaSummaryApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-summary";
    }

    @Override
    public String get() throws Throwable {
        return gson.toJson(Schematica.instance.getSummary());
    }

    @Override
    public String post(String body) throws Throwable {
        PostRequest request = gson.fromJson(body, PostRequest.class);
        if (request.action.equals("rescan")) {
            Schematica.instance.rescan(request.index);
        }
        if (request.action.equals("move")) {
            Schematica.instance.move(request.index, request.x, request.y, request.z);
        }
        return "{}";
    }

    @Override
    public String delete(String id) throws Throwable {
        if (id.equals("all")) {
            Schematica.instance.clear();
            return "{}";
        }

        Schematica.instance.remove(Integer.parseInt(id));
        return "{}";
    }

    public record PostRequest(String action, int index, int x, int y, int z) {}
}