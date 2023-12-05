package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.esp.*;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.scripting.*;
import com.zergatul.cheatutils.modules.utilities.DelayedRun;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.modules.visuals.*;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.FabricEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ModMain implements ClientModInitializer {

    public static final String MODID = "cheatutils";

    private final Logger logger = LogManager.getLogger(ModMain.class);

    private final List<Module> modules = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        FabricEvents.setup();

        ConfigHttpServer.instance.start();
        ConfigStore.instance.read();

        register(KeyBindingsController.instance);
        register(ChunkController.instance);
        register(NetworkPacketsController.instance);
        register(TeleportDetectorController.instance);
        register(SpeedCounterController.instance);

        register(AutoTotem.instance);
        register(KillAura.instance);
        register(AutoEat.instance);
        register(NoFall.instance);
        register(Scaffold.instance);

        register(LockInputsController.instance);
        register(AutoCraft.instance);
        register(WorldScannerController.instance);
        register(BlockEspController.instance);
        register(EntityEsp.instance);
        register(ProjectilePath.instance);
        register(EndCityChunks.instance);
        register(GameTickScriptingController.instance);
        register(AutoBucket.instance);
        register(WorldDownloadController.instance);
        register(EntityTitleController.instance);
        register(ContainerButtonsController.instance);
        register(TeleportHackController.instance);
        register(WorldMarkersController.instance);
        register(TpsCounterController.instance);
        register(ScriptedBlockPlacerController.instance);

        //register(FakeLag.instance);
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
        register(ShulkerTooltipController.instance);
        register(ArmorOverlay.instance);
        register(Fog.instance);
        register(AutoAttack.instance);
        register(Exec.instance);
        register(VillagerRoller.instance);
        register(AutoHotbar.instance);
        register(AreaMine.instance);
        register(ServerPlugins.instance);
        register(RenderUtilities.instance);
        register(DelayedRun.instance);

        Events.RegisterKeyBindings.trigger(KeyBindingHelper::registerKeyBinding);
    }

    private void register(Module module) {
        modules.add(module);
        logger.info("Registered: {}", module.getClass().getName());
    }

    private void register(Object instance) {
        logger.info("Registered: {}", instance.getClass().getName());
    }
}