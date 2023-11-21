package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.forge.ArmorGuiOverlay;
import com.zergatul.cheatutils.forge.BetterStatusEffectsGuiOverlay;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.esp.*;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.scripting.Exec;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.modules.visuals.AdvancedTooltips;
import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;
import com.zergatul.cheatutils.modules.visuals.Fog;
import com.zergatul.cheatutils.modules.visuals.Zoom;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.ForgeEvents;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    private static final Logger logger = LogManager.getLogger(ModMain.class);

    private final List<Module> modules = new ArrayList<>();

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlay);

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
        register(BedrockBreaker.instance);
        register(RenderUtilities.instance);
    }

    private void register(Module module) {
        modules.add(module);
        logger.info("Registered: {}", module.getClass().getName());
    }

    private void register(Object instance) {
        logger.info("Registered: {}", instance.getClass().getName());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());

        //MinecraftForge.EVENT_BUS.register(TestController.instance);

        //MinecraftForge.EVENT_BUS.register(AutoDropController.instance);
        //MinecraftForge.EVENT_BUS.register(LavaCastBuilderController.instance);

        // commands
        /*MinecraftForge.EVENT_BUS.register(GetSpeedCommand.instance);
        MinecraftForge.EVENT_BUS.register(WaitNextTickCommand.instance);
        MinecraftForge.EVENT_BUS.register(GetPositionCommand.instance);
        MinecraftForge.EVENT_BUS.register(WaitWorldLoadCommand.instance);
        MinecraftForge.EVENT_BUS.register(MoveToCommand.instance);*/
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        ConfigHttpServer.instance.start();
        ConfigStore.instance.read();
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        Events.RegisterKeyBindings.trigger(event::register);
    }

    private void onRegisterGuiOverlay(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("armor", new ArmorGuiOverlay());
        event.registerAboveAll("betterstatus", new BetterStatusEffectsGuiOverlay());
    }

    /*private static void multiply(String smallPath, String bigPath) {
        try {
            var input = new FileInputStream(smallPath);
            byte[] data = IOUtils.toByteArray(input);
            input.close();

            var small = new SchematicFile(data);
            var big = new SchematicFile(small.getWidth() * 2, small.getHeight() * 2, small.getLength() * 2);
            big.setPaletteEntry(49, Blocks.OBSIDIAN);
            small.copyTo(big, 0, 0, 0);
            small.copyTo(big, small.getWidth(), 0, 0);
            small.copyTo(big, 0, 0, small.getLength());
            small.copyTo(big, small.getWidth(), 0, small.getLength());
            small.copyTo(big, small.getWidth() / 2, small.getHeight(), small.getLength() / 2);

            var output = new FileOutputStream(bigPath);
            big.write(output);
            output.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }*/
}