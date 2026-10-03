package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.concurrent.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.font.FontBackendHolders;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.automation.schematica.Schematica;
import com.zergatul.cheatutils.modules.esp.*;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockFinder;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.scripting.*;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.modules.visuals.*;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;

public class Modules {

    public static void register() {
        register(ScriptExecutionManager.instance);
        register(ScriptRuntimeFailureHandler.instance);

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
        register(WorldDownload.INSTANCE);
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
        register(ContainerSummary.instance);
        register(CrystalAura.instance);
        register(FullBright.instance);
        register(InvMove.instance);
        register(HitboxSize.instance);
        register(BetterStatusEffects.instance);
        register(BoatHack.INSTANCE);
        register(Blink.instance);
        register(PigHack.INSTANCE);
        register(AutoDrop.INSTANCE);
        register(EditorConfig.INSTANCE);
        register(BobHurt.INSTANCE);
        register(AntiRespawnReset.INSTANCE);
        register(FastBreak.INSTANCE);
        register(Reach.INSTANCE);
        register(Performance.INSTANCE);
        register(McpServer.INSTANCE);
        register(StepUp.INSTANCE);
        register(BlockEntityDistance.INSTANCE);
        register(HandsView.INSTANCE);
        register(Core.INSTANCE);
        register(ChatUtilities.INSTANCE);
        register(ContainerButtons.INSTANCE);
        register(Chunks.INSTANCE);
        register(NewChunks.INSTANCE);
        register(UserName.INSTANCE);
        register(DeathCoordinates.INSTANCE);
        register(MiniMap.INSTANCE);
        register(ElytraTunnel.INSTANCE);
        register(Movement.INSTANCE);
        register(Debugging.INSTANCE);

        register(EventsScripting.instance);

        register(Privacy.instance);

        register(ClientTickEndExecutor.instance);
        register(InGameTickEndExecutor.instance);

        // new order independent modules
        //==========================================
        register(AfterPlayerAiStepExecutor.instance);
        register(AfterSendPlayerPosExecutor.instance);
        register(KillAura.instance);

        register(SpearRange.instance);
        register(AutoStunner.instance);
        register(BreachSwap.instance);
        //===========================================
    }

    public static void registerKeyBindings() {
        register(KeyBindings.instance);
    }

    public static void lateRegister() {
        register(EntityEsp.instance);
    }

    private static void register(Object instance) {}
}