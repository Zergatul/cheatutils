package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.modules.EntityEspConfig;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;

public class ConfigWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "entity-esp";
    }

    @Override
    public synchronized String get() {
        Object[] result;
        ImmutableList<EntityEspEntry> entries = EntityEsp.INSTANCE.getSettings().get().entries();
        result = entries.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws ApiException {
        Class<?> type = gson.fromJson(body, EntityEspEntry.UpdateOptions.class).type();

        SettingHandle2<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        if (handle.get().entries().contains(e -> e.type() == type)) {
            throw new ApiException("Entity config already exists.", HttpResponseCodes.BAD_REQUEST);
        }

        EntityEspEntry entry = EntityEspEntry.defaults(type);
        handle.update(config -> config.withEntries(config.entries().add(entry)));

        return gson.toJson(entry);
    }

    @Override
    public synchronized String put(String className, String body) throws ApiException {
        EntityEspEntry.UpdateOptions jsonConfig = gson.fromJson(body, EntityEspEntry.UpdateOptions.class);

        if (!className.equals(jsonConfig.type().getName())) {
            throw new ApiException("Entity class name don't match.", HttpResponseCodes.BAD_REQUEST);
        }

        SettingHandle2<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        if (!handle.get().entries().contains(e -> e.type() == jsonConfig.type())) {
            throw new ApiException("Cannot find entity config.", HttpResponseCodes.BAD_REQUEST);
        }

        handle.update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(e -> e.type() == jsonConfig.type(), e -> e.withOptions(jsonConfig));
            return config.withEntries(entries);
        });

        return gson.toJson(handle.get());
    }

    @Override
    public synchronized String delete(String className) {
        SettingHandle2<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        EntityEspEntry entry = handle.get().entries().stream()
                .filter(c -> c.type().getName().equals(className))
                .findFirst()
                .orElse(null);

        if (entry != null) {
            ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP)).remove(entry.type().getName());
            handle.update(config -> {
                ImmutableList<EntityEspEntry> entries = config.entries();
                entries = entries.removeAll(e -> e.type() == entry.type());
                return config.withEntries(entries);
            });
        }

        return "{ \"ok\": true }";
    }
}