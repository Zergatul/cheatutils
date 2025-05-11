package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.concurrent.PreRenderGuiExecutor;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.esp.*;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.scripting.*;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.modules.visuals.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Modules {

    private static final Logger LOGGER = LogManager.getLogger(Modules.class);

    public static void register() {
        register(BlockEventsProcessor.instance);
        register(NetworkPacketsController.instance);
        register(SpeedCounterController.instance);
        register(BlockFinder.instance);
        register(PreRenderGuiExecutor.instance);

        register(AutoTotem.instance);
        register(KillAura.instance);
        register(AutoEat.instance);
        register(NoFall.instance);
        register(Scaffold.instance);

        register(LockInputsController.instance);
        register(AutoCraft.instance);
        register(BlockEsp.instance);
        register(EntityEsp.instance);
        register(ProjectilePath.instance);
        register(EndCityChunks.instance);
        register(AutoBucket.instance);
        register(WorldDownloadController.instance);
        register(EntityTitleController.instance);
        register(ContainerButtonsController.instance);
        register(TeleportHackController.instance);
        register(WorldMarkersController.instance);
        register(TpsCounterController.instance);
        register(BlockAutomation.instance);
        register(PlayerInfoController.instance);

        register(FlyHack.instance);
        register(FreeCam.instance);
        register(AutoFish.instance);
        register(ChunkOverlayController.instance);
        register(StatusOverlay.instance);
        register(AutoCriticals.instance);
        register(LightLevel.instance);
        register(ElytraFly.instance);
        register(AdvancedTooltips.instance);
        register(Zoom.instance);
        register(ShulkerTooltip.instance);
        register(ArmorOverlay.instance);
        register(Fog.instance);
        register(AutoAttack.instance);
        register(Exec.instance);
        register(VillagerRoller.instance);
        register(AutoHotbar.instance);
        register(AreaMine.instance);
        register(ServerPlugins.instance);
        register(BedrockBreaker.instance);
        register(RenderUtilities.instance);
        register(Containers.instance);
        register(AntiHunger.instance);
        register(Schematica.instance);
        register(AimAssist.instance);
        register(LockInputs.instance);

        register(TickEndExecutor.instance);
    }

    public static void registerKeyBindings() {
        register(KeyBindingsController.instance);
    }

    private static void register(Module module) {
        LOGGER.debug("Registered module {}", module.getClass().getName());
    }

    private static void register(Object instance) {
        LOGGER.debug("Registered controller {}", instance.getClass().getName());
    }
}