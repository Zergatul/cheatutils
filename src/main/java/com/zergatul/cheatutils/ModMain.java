package com.zergatul.cheatutils;

import com.mojang.logging.LogUtils;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        ArmorOverlayController.instance.register();
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        ConfigHttpServer.instance.start();
        ConfigStore.instance.read();

        KeyBindingsController.instance.onRegister();
        MinecraftForge.EVENT_BUS.register(KeyBindingsController.instance);
        MinecraftForge.EVENT_BUS.register(ChunkController.instance);
        MinecraftForge.EVENT_BUS.register(FreeCamController.instance);
        MinecraftForge.EVENT_BUS.register(RenderController.instance);
        MinecraftForge.EVENT_BUS.register(NetworkPacketsController.instance);
        MinecraftForge.EVENT_BUS.register(WorldLoadController.instance);
        MinecraftForge.EVENT_BUS.register(TeleportController.instance);
        MinecraftForge.EVENT_BUS.register(ShulkerTooltipController.instance);
        MinecraftForge.EVENT_BUS.register(AutoFishController.instance);
        MinecraftForge.EVENT_BUS.register(LightLevelController.instance);
        MinecraftForge.EVENT_BUS.register(CustomCommandsController.instance);
        MinecraftForge.EVENT_BUS.register(ClientTickController.instance);
        MinecraftForge.EVENT_BUS.register(KillAuraController.instance);
        MinecraftForge.EVENT_BUS.register(DebugScreenController.instance);
        MinecraftForge.EVENT_BUS.register(EntitySpeedController.instance);
        MinecraftForge.EVENT_BUS.register(SpeedCounterController.instance);
        MinecraftForge.EVENT_BUS.register(AutoDisconnectController.instance);
        MinecraftForge.EVENT_BUS.register(ArmorOverlayController.instance);
        MinecraftForge.EVENT_BUS.register(BeeContainerController.instance);
        MinecraftForge.EVENT_BUS.register(ElytraHackController.instance);
        MinecraftForge.EVENT_BUS.register(EntityOwnerController.instance);
        MinecraftForge.EVENT_BUS.register(ExplorationMiniMapController.instance);
        MinecraftForge.EVENT_BUS.register(AutoCriticalsController.instance);
        MinecraftForge.EVENT_BUS.register(FlyHackController.instance);
        MinecraftForge.EVENT_BUS.register(AutoTotemController.instance);
        MinecraftForge.EVENT_BUS.register(MovementHackController.instance);
        MinecraftForge.EVENT_BUS.register(LockInputsController.instance);
        //MinecraftForge.EVENT_BUS.register(AutoWaterBucketController.instance);
        MinecraftForge.EVENT_BUS.register(ScaffoldController.instance);
        MinecraftForge.EVENT_BUS.register(AutoEatController.instance);
        MinecraftForge.EVENT_BUS.register(NoFallController.instance);
        MinecraftForge.EVENT_BUS.register(StatusOverlayController.instance);

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
}