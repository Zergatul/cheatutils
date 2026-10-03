package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ParkourAssistConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class ParkourAssist implements Module {

    public static final ParkourAssist instance = new ParkourAssist();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean enabled;

    private ParkourAssist() {
        Events.ModifyPlayerInput.add(this::onModifyPlayerInput);

        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    private void onModifyPlayerInput() {
        assert mc.level != null && mc.player != null;

        if (!enabled) {
            return;
        }

        if (!mc.player.onGround() || mc.player.getDeltaMovement().y > 0) {
            // jumping or falling
            return;
        }

        // copied from Entity.checkSupportingBlock
        double threshold = ConfigStore.instance.getConfig().parkourAssistConfig.threshold;
        AABB boundingBox = mc.player.getBoundingBox();
        AABB testArea = new AABB(boundingBox.minX, boundingBox.minY - threshold, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Optional<BlockPos> supportingBlock = mc.level.findSupportingBlock(mc.player, testArea);
        if (supportingBlock.isEmpty()) {
            mc.player.input.makeJump();
        }
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ParkourAssistConfig> {

        public WebApi() {
            super("parkour-assist", ParkourAssistConfig.class);
        }

        @Override
        protected ParkourAssistConfig getConfig() {
            return ConfigStore.instance.getConfig().parkourAssistConfig;
        }

        @Override
        protected void setConfig(ParkourAssistConfig config) {
            ConfigStore.instance.getConfig().parkourAssistConfig = config;
        }
    }
}