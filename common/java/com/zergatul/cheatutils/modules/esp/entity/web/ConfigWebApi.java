package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.SettingUpdateResult;
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
    public synchronized String post(String body) {
        Class<?> type = gson.fromJson(body, EntityEspEntry.UpdateOptions.class).type();

        SettingHandle<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        if (handle.get().entries().contains(e -> e.type() == type)) {
            throw new ApiException("Entity config already exists.", HttpResponseCodes.BAD_REQUEST);
        }

        EntityEspEntry entry = EntityEspEntry.defaults(type);
        handle.update(config -> config.withEntries(config.entries().add(entry)));

        return gson.toJson(entry);
    }

    @Override
    public synchronized String put(String className, String body) {
        EntityEspEntry.UpdateOptions jsonConfig = gson.fromJson(body, EntityEspEntry.UpdateOptions.class);

        if (!className.equals(jsonConfig.type().getName())) {
            throw new ApiException("Entity class name don't match.", HttpResponseCodes.BAD_REQUEST);
        }

        SettingHandle<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
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
    public String delete(String className) throws Exception {
        // Config and script change together on the client thread, which owns script state.
        return ClientThread.call(() -> {
            boolean removed = EntityEsp.INSTANCE.getSettings().updateWithResult(config -> {
                ImmutableList<EntityEspEntry> entries = config.entries().removeAll(e -> e.type().getName().equals(className));
                return new SettingUpdateResult<>(config.withEntries(entries), entries.size() != config.entries().size());
            });

            if (removed) {
                ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP)).remove(className);
            }

            return "{ \"ok\": true }";
        });
    }
}