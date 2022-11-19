package com.zergatul.cheatutils.scripting.api.overlay;

public class EntitiesApi {

    /*public boolean isEnabled(String className) {
        var config = getConfig(className);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    public int getCount(String className) {
        EntityUtils.EntityInfo info = EntityUtils.getEntityClass(className);
        if (info == null) {
            return Integer.MIN_VALUE;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return 0;
        }

        int count = 0;
        for (Entity entity: level.entitiesForRendering()) {
            if (info.clazz.isAssignableFrom(entity.getClass())) {
                count++;
            }
        }

        return count;
    }

    private EntityTracerConfig getConfig(String className) {
        var list = ConfigStore.instance.getConfig().entities.configs;
        return list.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
    }*/
}