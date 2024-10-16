package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.WorldDownloadController;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

public class WorldDownloadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "world-download";
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(new Status());
    }

    @Override
    public String post(String body) throws HttpException {
        body = gson.fromJson(body, String.class);

        if (body.startsWith("start:")) {
            try {
                WorldDownloadController.instance.start(body.substring(6));
                return get();
            }
            catch (Throwable e) {
                e.printStackTrace();
                throw new InternalServerErrorException(e.getMessage());
            }
        }

        if (body.equals("stop")) {
            WorldDownloadController.instance.stop();
            return get();
        }

        throw new MethodNotSupportedException("Invalid body");
    }

    public static class Status {
        public boolean active;

        public Status() {
            active = WorldDownloadController.instance.isActive();
        }
    }
}