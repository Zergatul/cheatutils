package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.esp.BlockFinder;
import org.apache.http.HttpException;

public class BlockEspRestartApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-esp-restart";
    }

    @Override
    public String post(String body) throws HttpException {
        ClientThreadDispatcher.run(BlockFinder.instance::restart);
        return "{}";
    }
}