package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.ServerPlugins;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class ServerPluginsApi {

    @MethodDescription("""
            Returns server plugin extracted from custom command list packet
            """)
    public String[] get() {
        return ServerPlugins.instance.getPlugins();
    }

    @MethodDescription("""
            Returns server plugin extracted from custom command list packet, but with bukkit:ver filter
            """)
    public String[] getBukkit() {
        return ServerPlugins.instance.getBukkitPlugins();
    }

    @MethodDescription("""
            Resets stored list of plugins, so module will request new lists once you call get()/getBukkit()
            """)
    public void reset() {
        ServerPlugins.instance.reset();
    }
}