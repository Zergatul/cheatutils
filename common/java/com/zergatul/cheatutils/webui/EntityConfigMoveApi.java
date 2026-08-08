package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import org.apache.http.HttpException;

public class EntityConfigMoveApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities-move";
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = gson.fromJson(body, Request.class);
        if (request.clazz == null) {
            return gson.toJson(new Response(false, "Class is null"));
        }

        boolean up = request.direction.equals("up");
        boolean down = request.direction.equals("down");
        if (!up && !down) {
            return gson.toJson(new Response(false, "Invalid direction"));
        }

        ImmutableList<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
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

        ImmutableList<EntityTracerConfig> updated = up ? list.swap(index, index - 1) : list.swap(index, index + 1);
        ConfigStore.updateFromApi(c -> c.entities, entities -> entities.configs = updated);

        return gson.toJson(new Response(true, null));
    }

    public record Request(String direction, Class clazz) {}

    public record Response(boolean ok, String message) {}
}