package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModMain implements ClientModInitializer {

    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    @Override
    public void onInitializeClient() {
        FabricEvents.setup();

        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.registerKeyBindings();
        Modules.register();
        Events.RegisterKeyBindings.trigger(KeyBindingHelper::registerKeyBinding);

        /*NetworkPacketsController.instance.addClientPacketHandler(args -> {
            if (args.packet instanceof ServerboundMovePlayerPacket movePlayerPacket) {
                if (movePlayerPacket.hasRotation()) {
                    if (prevYRot == -135.1f) {
                        LOGGER.debug("111");
                    }
                    prevYRot = movePlayerPacket.getYRot(0);
                    LOGGER.info("Tick#{} xRot: {} yRot: {}",
                            Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0,
                            String.format("%.3f", movePlayerPacket.getXRot(0)),
                            String.format("%.3f", movePlayerPacket.getYRot(0)));
                }
            }
        });*/
    }

    private static float prevYRot = 0;
}