package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.esp.BlockFinder;
import org.apache.http.HttpException;

public class RescanChunksApi extends ApiBase {

    @Override
    public String getRoute() {
        return "rescan-chunks";
    }

    @Override
    public String post(String body) throws HttpException {
        BlockFinder.instance.rescan();
        return "{}";
    }
}