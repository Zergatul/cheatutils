package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import org.apache.http.MethodNotSupportedException;

public class EntitiesConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities";
    }

    @Override
    public synchronized String get() throws MethodNotSupportedException {
        Object[] result;
        var list = ConfigStore.instance.getConfig().entities.configs;
        result = list.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws MethodNotSupportedException {
        EntityEspConfig jsonConfig = gson.fromJson(body, EntityEspConfig.class);

        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz == jsonConfig.clazz)
                .findFirst()
                .orElse(null);
        if (config != null) {
            throw new MethodNotSupportedException("Entity config already exists.");
        }

        config = EntityEspConfig.createDefault(jsonConfig.clazz);
        ConfigStore.instance.getConfig().entities.add(config);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String put(String className, String body) throws MethodNotSupportedException {
        EntityEspConfig jsonConfig = gson.fromJson(body, EntityEspConfig.class);
        String obfClassName = ClassRemapper.toObf(className);
        if (!obfClassName.equals(jsonConfig.clazz.getName())) {
            throw new MethodNotSupportedException("Entity class name don't match.");
        }

        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz == jsonConfig.clazz)
                .findFirst()
                .orElse(null);
        if (config == null) {
            throw new MethodNotSupportedException("Cannot find entity config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String delete(String className) throws MethodNotSupportedException {
        String obfClassName = ClassRemapper.toObf(className);
        EntityEspConfig config = ConfigStore.instance.getConfig().entities.configs.stream()
                .filter(c -> c.clazz.getName().equals(obfClassName))
                .findFirst()
                .orElse(null);

        ConfigStore.instance.getConfig().entities.remove(config);
        ConfigStore.instance.requestWrite();

        return "{ ok: true }";
    }
}