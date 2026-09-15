package com.zergatul.cheatutils.webui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class EntityTypesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entity-types";
    }

    @Override
    public String get() {
        return gson.toJson(BuiltInRegistries.ENTITY_TYPE.keySet()
                .stream()
                .map(Identifier::toString)
                .toArray(String[]::new));
    }
}