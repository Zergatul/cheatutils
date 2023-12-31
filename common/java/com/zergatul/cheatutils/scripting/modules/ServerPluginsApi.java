package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.ServerPlugins;
import com.zergatul.cheatutils.scripting.HelpText;

public class ServerPluginsApi {

    public String[] get() {
        return ServerPlugins.instance.getPlugins();
    }

    public String[] getBukkit() {
        return ServerPlugins.instance.getBukkitPlugins();
    }

    @HelpText("Resets stored list of plugins, so module will request new lists once you call get()/getBukkit()")
    public void reset() {
        ServerPlugins.instance.reset();
    }
}