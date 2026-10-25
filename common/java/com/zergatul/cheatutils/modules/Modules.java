package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.concurrent.*;
import com.zergatul.cheatutils.controllers.*;
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

        register(AutoTotem.INSTANCE);
        register(AutoEat.instance);
        register(NoFall.instance);
        register(Scaffold.instance);
        register(ElytraBounce.instance);
        register(ParkourAssist.instance);

        register(AutoCraft.instance);
        register(BlockEsp.instance);
        register(ProjectilePath.instance);
        register(EndCityChunks.INSTANCE);
        register(AutoBucket.instance);
        register(WorldDownload.INSTANCE);
        register(EntityTitle.instance);
        register(TeleportHack.instance);
        register(WorldMarkers.INSTANCE);
        register(TpsCounterController.instance);
        register(BlockAutomation.instance);
        register(PlayerInfoController.instance);

        register(FlyHack.INSTANCE);
        register(FreeCam.instance);
        register(AutoFish.INSTANCE);
        register(ChunkOverlayController.instance);
        register(StatusOverlay.INSTANCE);
        register(AutoCriticals.INSTANCE);
        register(LightLevel.INSTANCE);
        register(ElytraFly.instance);
        register(AdvancedTooltips.INSTANCE);
        register(Zoom.instance);
        register(ShulkerTooltip.INSTANCE);
        register(ArmorOverlay.INSTANCE);
        register(Fog.INSTANCE);
        register(AutoAttack.instance);

        register(Exec.INSTANCE);
        register(VillagerRoller.instance);
        register(AutoHotbar.instance);
        register(AreaMine.INSTANCE);
        register(ServerPlugins.instance);
        register(BedrockBreaker.instance);
        register(Containers.instance);
        register(AntiHunger.INSTANCE);
        register(Schematica.INSTANCE);
        register(AimAssist.instance);
        register(LockInputs.instance);
        register(LogoutSpots.instance);
        register(AutoTool.instance);
        register(AirPlace.INSTANCE);
        register(ContainerSummary.instance);
        register(CrystalAura.INSTANCE);
        register(FullBright.INSTANCE);
        register(InvMove.INSTANCE);
        register(HitboxSize.instance);
        register(BetterStatusEffects.INSTANCE);
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

        register(Privacy.INSTANCE);

        register(ClientTickEndExecutor.instance);
        register(InGameTickEndExecutor.instance);

        // new order independent modules
        //==========================================
        register(AfterPlayerAiStepExecutor.instance);
        register(AfterSendPlayerPosExecutor.instance);
        register(KillAura.INSTANCE);

        register(SpearRange.INSTANCE);
        register(AutoStunner.INSTANCE);
        register(BreachSwap.INSTANCE);
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