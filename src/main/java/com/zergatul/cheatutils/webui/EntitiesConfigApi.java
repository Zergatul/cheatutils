package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.JsonEntityTracerConfig;
import org.apache.http.MethodNotSupportedException;

public class EntitiesConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        Object[] result;
        synchronized (ConfigStore.instance.entities) {
            result = ConfigStore.instance.entities.stream().map(c -> c.convert()).toArray();
        }
        return gson.toJson(result);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {

        JsonEntityTracerConfig jsonConfig = gson.fromJson(body, JsonEntityTracerConfig.class);

        Class clazz;
        try {
            clazz = Class.forName(jsonConfig.className);
        } catch (ClassNotFoundException e) {
            throw new MethodNotSupportedException("Class doesn't exist.");
        }

        EntityTracerConfig config;
        synchronized (ConfigStore.instance.entities) {

            config = ConfigStore.instance.entities.stream().filter(c -> c.clazz == clazz).findFirst().orElse(null);
            if (config != null) {
                throw new MethodNotSupportedException("Entity config already exists.");
            }

            config = EntityTracerConfig.createDefault(clazz);
            ConfigStore.instance.addEntity(config);
        }

        ConfigStore.instance.write();

        return gson.toJson(config.convert());
    }

    @Override
    public String put(String className, String body) throws MethodNotSupportedException {

        JsonEntityTracerConfig jsonConfig = gson.fromJson(body, JsonEntityTracerConfig.class);
        if (!className.equals(jsonConfig.className)) {
            throw new MethodNotSupportedException("Entity class name don't match.");
        }

        EntityTracerConfig config;
        synchronized (ConfigStore.instance.entities) {
            config = ConfigStore.instance.entities.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
        }

        if (config == null) {
            throw new MethodNotSupportedException("Cannot find entity config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.write();

        return gson.toJson(config.convert());
    }

    @Override
    public String delete(String className) throws MethodNotSupportedException {

        synchronized (ConfigStore.instance.entities) {
            EntityTracerConfig config = ConfigStore.instance.entities.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
            if (config == null) {
                throw new MethodNotSupportedException("Cannot find entity config.");
            }

            ConfigStore.instance.removeEntity(config);
        }

        ConfigStore.instance.write();

        return "{ ok: true }";
    }

}