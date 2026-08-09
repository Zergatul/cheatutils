package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
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
        register(KeyBindings.instance);
        register(ChunkController.instance);
        register(NetworkPacketsController.instance);
        register(TeleportDetectorController.instance);
        register(SpeedCounterController.instance);
        register(BlockFinder.instance);
        register(FakeRotation.instance);

        register(AutoTotem.instance);
        register(KillAura.instance);
        register(AutoEat.instance);
        register(NoFall.instance);
        register(Scaffold.instance);

        register(LockInputs.instance);
        register(AutoCraft.instance);
        register(WorldScannerController.instance);
        register(BlockEsp.instance);
        register(EntityEsp.instance);
        register(ProjectilePath.instance);
        register(EndCityChunks.instance);
        register(GameTickScriptingController.instance);
        register(AutoBucket.instance);
        register(WorldDownloadController.instance);
        register(EntityTitle.instance);
        register(ContainerButtonsController.instance);
        register(TeleportHack.instance);
        register(WorldMarkers.instance);
        register(TpsCounterController.instance);
        register(BlockAutomation.instance);

        // register(FakeLag.instance);
        register(FlyHack.instance);
        register(FreeCam.instance);
        register(AutoFish.instance);
        register(ChunkOverlayController.instance);
        register(StatusOverlay.instance);
        register(AutoCriticals.instance);
        register(LightLevel.instance);
        register(ElytraFly.instance);
        register(AdvancedTooltips.instance);
        register(AutoDisconnect.instance);
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
        register(ContainerSummary.instance);
        register(Schematica.instance);
        register(ClientTickEndExecutor.instance);
    }

    private static void register(Module module) {
        LOGGER.debug("Registered module {}", module.getClass().getName());
    }

    private static void register(Object instance) {
        LOGGER.debug("Registered controller {}", instance.getClass().getName());
    }
}