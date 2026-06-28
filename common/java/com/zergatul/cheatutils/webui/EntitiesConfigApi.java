package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;

public class EntitiesConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities";
    }

    @Override
    public synchronized String get() {
        Object[] result;
        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        result = list.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws ApiException {
        EntityEspConfig jsonConfig = gson.fromJson(body, EntityEspConfig.class);
        jsonConfig.validate();

        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz == jsonConfig.clazz)
                .findFirst()
                .orElse(null);
        if (config != null) {
            throw new ApiException("Entity config already exists.", HttpResponseCodes.BAD_REQUEST);
        }

        config = EntityEspConfig.createDefault(jsonConfig.clazz);
        ConfigStore.instance.getConfig().entities.add(config);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String put(String className, String body) throws ApiException {
        EntityEspConfig jsonConfig = gson.fromJson(body, EntityEspConfig.class);
        jsonConfig.validate();

        if (!className.equals(jsonConfig.clazz.getName())) {
            throw new ApiException("Entity class name don't match.", HttpResponseCodes.BAD_REQUEST);
        }

        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz == jsonConfig.clazz)
                .findFirst()
                .orElse(null);
        if (config == null) {
            throw new ApiException("Cannot find entity config.", HttpResponseCodes.BAD_REQUEST);
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String delete(String className) {
        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz.getName().equals(className))
                .findFirst()
                .orElse(null);

        if (config != null) {
            ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP)).remove(config.clazz.getName());
            ConfigStore.instance.getConfig().entities.remove(config);
        }
        ConfigStore.instance.requestWrite();

        return "{ \"ok\": true }";
    }
}