package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import org.apache.http.MethodNotSupportedException;

public class EntitiesConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "entities";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        Object[] result;
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            result = list.stream().toArray();
        }
        return gson.toJson(result);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {

        EntityTracerConfig jsonConfig = gson.fromJson(body, EntityTracerConfig.class);

        EntityTracerConfig config;
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {

            config = list.stream().filter(c -> c.clazz == jsonConfig.clazz).findFirst().orElse(null);
            if (config != null) {
                throw new MethodNotSupportedException("Entity config already exists.");
            }

            config = EntityTracerConfig.createDefault(jsonConfig.clazz);
            list.add(config);
        }

        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public String put(String className, String body) throws MethodNotSupportedException {

        EntityTracerConfig jsonConfig = gson.fromJson(body, EntityTracerConfig.class);
        if (!className.equals(jsonConfig.clazz.getName())) {
            throw new MethodNotSupportedException("Entity class name don't match.");
        }

        EntityTracerConfig config;
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            config = list.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
        }

        if (config == null) {
            throw new MethodNotSupportedException("Cannot find entity config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public String delete(String className) throws MethodNotSupportedException {

        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            EntityTracerConfig config = list.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
            if (config == null) {
                throw new MethodNotSupportedException("Cannot find entity config.");
            }

            list.remove(config);
        }

        ConfigStore.instance.requestWrite();

        return "{ ok: true }";
    }

}