package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HitboxSizeConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class HitboxSize implements Module {

    public static final HitboxSize instance = new HitboxSize();

    private HitboxSize() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public AABB get(Entity entity) {
        HitboxSizeConfig config = ConfigStore.instance.getConfig().hitboxSizeConfig;
        if (config.enabled) {
            Vec3 pos = entity.position();
            double width = entity.getBbWidth();
            double height = entity.getBbHeight();
            width = apply(width, config.widthMode, config.widthPercent, config.widthAbsolute);
            height = apply(height, config.heightMode, config.heightPercent, config.heightAbsolute);
            width /= 2;
            return new AABB(
                    pos.x - width, pos.y, pos.z - width,
                    pos.x + width, pos.y + height, pos.z + width);
        } else {
            return entity.getBoundingBox();
        }
    }

    private double apply(double value, String mode, double percent, double absolute) {
        if (Objects.equals(mode, HitboxSizeConfig.MODE_PERCENT)) {
            return value * (1 + percent / 100);
        }
        if (Objects.equals(mode, HitboxSizeConfig.MODE_ABSOLUTE)) {
            return value + absolute;
        }
        return value;
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<HitboxSizeConfig> {

        public WebApi() {
            super("hitbox-size", HitboxSizeConfig.class);
        }

        @Override
        protected HitboxSizeConfig getConfig() {
            return ConfigStore.instance.getConfig().hitboxSizeConfig;
        }

        @Override
        protected void setConfig(HitboxSizeConfig config) {
            ConfigStore.instance.getConfig().hitboxSizeConfig = config;
        }
    }
}