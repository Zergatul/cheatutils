package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;

import java.util.Objects;

public class EntityConfigMoveApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities-move";
    }

    @Override
    public String post(String body) {
        Request request = gson.fromJson(body, Request.class);
        if (request.clazz == null) {
            return gson.toJson(new Response(false, "Class is null"));
        }

        boolean up = request.direction.equals("up");
        boolean down = request.direction.equals("down");
        if (!up && !down) {
            return gson.toJson(new Response(false, "Invalid direction"));
        }

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        int index = list.indexOf(c -> c.clazz == request.clazz);
        if (index < 0) {
            return gson.toJson(new Response(false, "Cannot find class in list"));
        }

        if (index == 0 && up) {
            return gson.toJson(new Response(true, "Cannot move up"));
        }

        if (index == list.size() - 1 && down) {
            return gson.toJson(new Response(true, "Cannot move down"));
        }

        if (up) {
            ConfigStore.instance.getConfig().entities.configs = list.swap(index, index - 1);
        } else {
            ConfigStore.instance.getConfig().entities.configs = list.swap(index, index + 1);
        }
        ConfigStore.instance.requestWrite();

        return gson.toJson(new Response(true, null));
    }

    public static final class Request {
        public String direction;
        public Class<?> clazz;
    }

    public static class Response {

        public boolean ok;
        public String message;

        public Response(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }
    }
}