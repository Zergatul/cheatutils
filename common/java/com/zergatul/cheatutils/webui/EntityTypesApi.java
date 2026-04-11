package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.resources.Identifier;

public class EntityTypesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entity-types";
    }

    @Override
    public String get() {
        String[] types = Registries.ENTITY_TYPES.getValues().stream()
                .map(Registries.ENTITY_TYPES::getKey)
                .map(Identifier::toString)
                .toArray(String[]::new);
        return gson.toJson(types);
    }
}