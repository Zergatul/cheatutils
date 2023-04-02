package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ClassRemapper;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.interfaces.ClientWorldMixinInterface;
import com.zergatul.cheatutils.utils.EntityUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class EntitiesApi {

    public boolean isEnabled(String className) {
        var config = getConfig(className);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @SuppressWarnings("unchecked")
    public int getCount(String className) {
        EntityUtils.EntityInfo info = EntityUtils.getEntityClass(ClassRemapper.toObf(className));
        if (info == null) {
            return Integer.MIN_VALUE;
        }

        ClientWorld level = MinecraftClient.getInstance().world;
        if (level == null) {
            return 0;
        }

        int count = 0;
        for (Entity entity: ((ClientWorldMixinInterface) level).getEntityManager().getLookup().iterate()) {
            if (info.clazz.isAssignableFrom(entity.getClass())) {
                count++;
            }
        }

        return count;
    }

    private EntityTracerConfig getConfig(String className) {
        var list = ConfigStore.instance.getConfig().entities.configs;
        return list.stream()
                .filter(c -> c.clazz.getName().equals(ClassRemapper.toObf(className)))
                .findFirst()
                .orElse(null);
    }
}