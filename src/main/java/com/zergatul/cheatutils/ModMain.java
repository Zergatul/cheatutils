package com.zergatul.cheatutils;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.KeyBindingsController;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.controllers.RenderController;
import com.zergatul.cheatutils.controllers.TeleportController;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModMain implements ClientModInitializer {

	private final Logger logger = LogManager.getLogger(ModMain.class);

	@Override
	public void onInitializeClient() {
		ModApiWrapper.setup();

		ConfigHttpServer.instance.start();
		ConfigStore.instance.read();

		register(NetworkPacketsController.instance);
		register(KeyBindingsController.instance);
		register(RenderController.instance);
		register(TeleportController.instance);

		ModApiWrapper.triggerOnRegisterKeyBindings(KeyBindingHelper::registerKeyBinding);
	}

	private void register(Object instance) {
		logger.info("Registered: {}", instance.getClass().getName());
	}
}