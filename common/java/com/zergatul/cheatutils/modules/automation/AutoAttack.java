package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoAttackConfig;
import com.zergatul.cheatutils.mixins.common.accessors.MinecraftAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;

public class AutoAttack implements Module {

    public static final AutoAttack INSTANCE = new AutoAttack();

    private final SettingHandle2<AutoAttackConfig> settings = Settings.get(AutoAttackConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private int nextExtraTicks = Integer.MIN_VALUE;

    private AutoAttack() {
        Events.InGameTickEnd.add(this::onClientTickEnd);

        WebApiRegistry.INSTANCE.register("auto-attack", settings, AutoAttackConfig.class);
    }

    public SettingHandle2<AutoAttackConfig> getSettings() {
        return settings;
    }

    private void onClientTickEnd() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        AutoAttackConfig config = settings.get();
        if (!config.enabled()) {
            return;
        }

        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        if (mc.hitResult == null) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        calculateNextExtraTicksIfRequired(config);

        if (mc.player.getAttackStrengthScale(-nextExtraTicks) != 1) {
            return;
        }

        if (config.limitRange() && mc.hitResult.getLocation().distanceToSqr(mc.player.getEyePosition()) > config.maxRange() * config.maxRange()) {
            return;
        }

        nextExtraTicks = Integer.MIN_VALUE;

        ((MinecraftAccessor) mc).startAttack_CU();
    }

    private void calculateNextExtraTicksIfRequired(AutoAttackConfig config) {
        if (nextExtraTicks == Integer.MIN_VALUE) {
            nextExtraTicks = config.extraTicksMin() + (int) Math.floor(Math.random() * (config.extraTicksMax() - config.extraTicksMin() + 1));
        }
    }
}