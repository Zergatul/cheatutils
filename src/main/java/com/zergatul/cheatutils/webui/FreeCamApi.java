package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.Minecraft;

/** Runtime activation is separate from persisted FreeCam settings. */
public class FreeCamApi extends ApiBase {
    @Override
    public String getRoute() {
        return "free-cam-state";
    }

    @Override
    public String get() {
        State state = new State();
        state.active = FreeCam.instance.isActive();
        Minecraft mc = Minecraft.getMinecraft();
        state.available = mc.world != null && mc.player != null && !mc.player.isDead;
        return gson.toJson(state);
    }

    @Override
    public String post(String body) throws ApiException {
        String command = gson.fromJson(body, String.class);
        if (command == null) throw new ApiException("Command is required.", 400);
        switch (command) {
            case "toggle": FreeCam.instance.toggle(); break;
            case "enable": FreeCam.instance.enable(); break;
            case "disable": FreeCam.instance.disable(); break;
            default: throw new ApiException("Unsupported command.", 400);
        }
        return get();
    }

    private static class State {
        boolean active;
        boolean available;
    }
}
