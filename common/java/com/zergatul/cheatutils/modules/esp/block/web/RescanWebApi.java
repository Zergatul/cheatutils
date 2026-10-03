package com.zergatul.cheatutils.modules.esp.block.web;

import com.zergatul.cheatutils.modules.esp.block.BlockFinder;
import com.zergatul.cheatutils.web.WebApiBase;

public class RescanWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "rescan-chunks";
    }

    @Override
    public String post(String body) {
        BlockFinder.instance.rescan();
        return "{}";
    }
}