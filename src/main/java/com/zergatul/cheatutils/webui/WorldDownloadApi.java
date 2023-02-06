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
    public String post(String body) throws HttpException {
        if (body.startsWith("begin:")) {
            WorldDownloadController.instance.begin(body.substring(6));
            return "{ \"ok\": true }";
        }
        if (body.equals("end")) {
            WorldDownloadController.instance.end();
            return "{ \"ok\": true }";
        }

        throw new MethodNotSupportedException("Invalid body");
    }
}