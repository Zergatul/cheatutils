package com.zergatul.cheatutils;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.schematics.SchematicFile;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;

@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    private static final Logger logger = LogManager.getLogger(ModMain.class);

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlay);

        register(KeyBindingsController.instance);
        register(ChunkController.instance);
        register(NetworkPacketsController.instance);
        register(TeleportController.instance);
        register(AutoTotemController.instance);
        register(KillAuraController.instance);
        register(AutoEatController.instance);
        //register(AutoPlacerController.instance);
        register(NoFallController.instance);
        register(ScaffoldController.instance);
        register(SpeedCounterController.instance);
        register(LockInputsController.instance);
        register(AutoCraftController.instance);
        register(WorldScannerController.instance);
        register(BlockEspController.instance);
        register(EntityEspController.instance);
        register(ProjectilePathController.instance);
        register(EndCityChunksController.instance);
        register(GameTickScriptingController.instance);
        register(AutoBucketController.instance);
        register(WorldDownloadController.instance);
        register(EntityTitleController.instance);
        register(ContainerButtonsController.instance);
    }

    private void register(Object instance) {
        logger.info("Registered: {}", instance.getClass().getName());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ModApiWrapper.forgeEvents);

        MinecraftForge.EVENT_BUS.register(FreeCamController.instance);
        MinecraftForge.EVENT_BUS.register(ShulkerTooltipController.instance);
        MinecraftForge.EVENT_BUS.register(AutoFishController.instance);
        MinecraftForge.EVENT_BUS.register(LightLevelController.instance);
        MinecraftForge.EVENT_BUS.register(DebugScreenController.instance);
        MinecraftForge.EVENT_BUS.register(EntitySpeedController.instance);
        MinecraftForge.EVENT_BUS.register(AutoDisconnectController.instance);
        MinecraftForge.EVENT_BUS.register(ArmorOverlayController.instance);
        MinecraftForge.EVENT_BUS.register(ElytraHackController.instance);
        MinecraftForge.EVENT_BUS.register(AutoCriticalsController.instance);
        MinecraftForge.EVENT_BUS.register(FlyHackController.instance);
        MinecraftForge.EVENT_BUS.register(AdvancedTooltipsController.instance);
        MinecraftForge.EVENT_BUS.register(InstantDisconnectController.instance);
        MinecraftForge.EVENT_BUS.register(BeaconController.instance);
        MinecraftForge.EVENT_BUS.register(StatusOverlayController.instance);
        MinecraftForge.EVENT_BUS.register(ChunkOverlayController.instance);
        MinecraftForge.EVENT_BUS.register(ZoomController.instance);

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
        ModApiWrapper.RegisterKeyBindings.trigger(event::register);
    }

    private void onRegisterGuiOverlay(final RegisterGuiOverlaysEvent event) {
        ArmorOverlayController.instance.onRegister(event);
        BetterStatusEffectsController.instance.onRegister(event);
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