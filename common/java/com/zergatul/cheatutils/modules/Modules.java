package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.concurrent.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.font.FontBackendHolders;
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

        //Order dependent modules -> legacy method, use Event.event.add(function, priority) for new modules, use the priority value instead
        //==========================

        //===========================

        register(FakeRotation.instance);
        register(BlockEventsProcessor.instance);
        register(NetworkPacketsController.instance);
        register(SpeedCounterController.instance);
        register(BlockFinder.instance);
        register(PreRenderGuiExecutor.instance);

        register(AutoTotem.instance);
        register(AutoEat.instance);
        register(NoFall.instance);
        register(Scaffold.instance);
        register(ElytraBounce.instance);
        register(ParkourAssist.instance);

        register(LockInputsController.instance);
        register(AutoCraft.instance);
        register(BlockEsp.instance);
        register(ProjectilePath.instance);
        register(EndCityChunks.instance);
        register(AutoBucket.instance);
        register(WorldDownloadController.instance);
        register(EntityTitle.instance);
        register(ContainerButtonsController.instance);
        register(TeleportHack.instance);
        register(WorldMarkers.instance);
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
        register(Containers.instance);
        register(AntiHunger.instance);
        register(Schematica.instance);
        register(AimAssist.instance);
        register(LockInputs.instance);
        register(LogoutSpots.instance);
        register(AutoTool.instance);
        register(AirPlace.instance);
//        register(Physics.instance);
        register(ContainerSummary.instance);
        register(CrystalAura.instance);


        register(TickEndExecutor.instance);

        // new order independent modules
        //==========================================
        register(AfterPlayerAiStepExecutor.instance);
        register(AfterSendPlayerPosExecutor.instance);
        register(KillAura.instance);

        register(SpearRange.instance);
        register(AutoStunner.instance);
        register(BreachSwap.instance);
        //===========================================

        FontBackendHolders.add(StatusOverlay.instance);
        FontBackendHolders.add(EntityTitle.instance);
        FontBackendHolders.add(WorldMarkers.instance);
    }

    public static void registerKeyBindings() {
        register(KeyBindingsController.instance);
    }

    public static void lateRegister() {
        register(EntityEsp.instance);
    }

    private static void register(Module module) {
        LOGGER.debug("Registered module {}", module.getClass().getName());
    }

    private static void register(Object instance) {
        LOGGER.debug("Registered controller {}", instance.getClass().getName());
    }
}