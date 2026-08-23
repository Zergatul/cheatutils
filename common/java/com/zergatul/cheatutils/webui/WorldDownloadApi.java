package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.WorldDownloadController;
import org.apache.http.HttpException;

public class WorldDownloadApi extends ApiBase {

    @Override
    public String getRoute() {
        return "world-download";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(new Status());
    }

    @Override
    public String post(String body) throws HttpException {
        String command = WebHelper.parseJson(gson, body, String.class);

        if (command.startsWith("start:")) {
            try {
                WorldDownloadController.instance.start(command.substring(6));
                return get();
            }
            catch (Throwable e) {
                throw new ApiException(e.getMessage(), HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
            }
        }

        if (command.equals("stop")) {
            WorldDownloadController.instance.stop();
            return get();
        }

        throw new ApiException("Invalid body", HttpResponseCodes.BAD_REQUEST);
    }

    public static class Status {
        public boolean active;

        public Status() {
            active = WorldDownloadController.instance.isActive();
        }
    }
}