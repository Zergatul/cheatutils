package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.hacks.ServerPlugins;

public class ServerPluginsApi {

    public String[] get() {
        return ServerPlugins.instance.getPlugins();
    }

    public String[] getBukkit() {
        return ServerPlugins.instance.getBukkitPlugins();
    }
}