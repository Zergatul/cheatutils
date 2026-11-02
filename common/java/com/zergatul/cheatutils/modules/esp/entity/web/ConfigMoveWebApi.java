package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.modules.EntityEspConfig;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.web.WebApiBase;

public class ConfigMoveWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "entity-esp-move";
    }

    @Override
    public String post(String body) {
        Request request = gson.fromJson(body, Request.class);
        if (request.type == null) {
            return gson.toJson(new Response(false, "Class is null"));
        }

        boolean up = request.direction.equals("up");
        boolean down = request.direction.equals("down");
        if (!up && !down) {
            return gson.toJson(new Response(false, "Invalid direction"));
        }

        SettingHandle2<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        ImmutableList<EntityEspEntry> entries = handle.get().entries();
        int index = entries.indexOf(c -> c.type() == request.type);
        if (index < 0) {
            return gson.toJson(new Response(false, "Cannot find class in list"));
        }

        if (index == 0 && up) {
            return gson.toJson(new Response(true, "Cannot move up"));
        }

        if (index == entries.size() - 1 && down) {
            return gson.toJson(new Response(true, "Cannot move down"));
        }

        if (up) {
            handle.update(config -> config.withEntries(config.entries().swap(index, index - 1)));
        } else {
            handle.update(config -> config.withEntries(config.entries().swap(index, index + 1)));
        }

        return gson.toJson(new Response(true, null));
    }

    public record Request(String direction, Class<?> type) {}

    public record Response(boolean ok, String message) {}
}