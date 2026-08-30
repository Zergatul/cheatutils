package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;

import java.util.Base64;

public class SchematicaDownloadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-download";
    }

    @Override
    public String post(String body) {
        Request request = gson.fromJson(body, Request.class);
        Schematica.DownloadInfo info = Schematica.instance.download(
                request.format,
                request.getX1(), request.getY1(), request.getZ1(),
                request.getX2(), request.getY2(), request.getZ2());
        if (info.data() != null) {
            return gson.toJson(new Response(Base64.getEncoder().encodeToString(info.data()), null));
        }
        return gson.toJson(new Response(null, info.error()));
    }

    public static class Request {

        public String format;
        public int x1, y1, z1, x2, y2, z2;

        public int getX1() { return Math.min(x1, x2); }
        public int getX2() { return Math.max(x1, x2); }
        public int getY1() { return Math.min(y1, y2); }
        public int getY2() { return Math.max(y1, y2); }
        public int getZ1() { return Math.min(z1, z2); }
        public int getZ2() { return Math.max(z1, z2); }
    }

    public record Response(String data, String error) {}
}