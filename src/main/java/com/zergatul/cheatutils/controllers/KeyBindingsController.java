package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindingsController {

    public static final KeyBindingsController instance = new KeyBindingsController();
    public static final String category = "Zergatul Cheat Utils";

    public static KeyMapping toggleEsp = new KeyMapping("Toggle ESP", GLFW.GLFW_KEY_BACKSLASH, category);
    //public static KeyMapping openConfig = new KeyMapping("Copy config URL", GLFW.GLFW_KEY_Z, category);
    //public static KeyMapping quickCommand = new KeyMapping("Quick command", GLFW.GLFW_KEY_X, category);
    public static KeyMapping toggleFreeCam = new KeyMapping("Toggle free cam", GLFW.GLFW_KEY_F6, category);

    private Minecraft mc = Minecraft.getInstance();

    private KeyBindingsController() {

    }

    public void setup() {
        ClientRegistry.registerKeyBinding(toggleEsp);
        //ClientRegistry.registerKeyBinding(openConfig);
        //ClientRegistry.registerKeyBinding(quickCommand);
        ClientRegistry.registerKeyBinding(toggleFreeCam);
    }

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {

        if (mc.player == null) {
            return;
        }
        if (mc.screen != null) {
            return;
        }

        /*if (KeyBindingsController.openConfig.isDown()) {
            if (!HardSwitchController.instance.isTurnedOff()) {
                String uri = ConfigHttpServer.instance.getUrl();
                if (uri != null) {
                    new ClipboardHelper().setClipboard(mc.getWindow().getWindow(), uri);
                }
            }
            return;
        }*/

        if (KeyBindingsController.toggleEsp.isDown()) {
            if (!HardSwitchController.instance.isTurnedOff()) {
                ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
                ConfigStore.instance.requestWrite();
            }
            return;
        }

        /*if (KeyBindingsController.quickCommand.isDown()) {
            if (!HardSwitchController.instance.isTurnedOff()) {
                mc.player.chat("/home");
            }
            return;
        }*/

        if (KeyBindingsController.toggleFreeCam.isDown()) {
            if (!HardSwitchController.instance.isTurnedOff()) {
                FreeCamController.instance.toggle();
            }
            return;
        }
    }
}
