package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.scripting.ScriptingRuntimeSmokeTest;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.FabricEvents;
import com.zergatul.cheatutils.wrappers.ModEnvironment;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class ModMain implements ClientModInitializer {

	public static final String MODID = "cheatutils";

	@Override
	public void onInitializeClient() {
		FabricEvents.setup();

		Profiles.instance.init();
		ConfigHttpServer.instance.start();
		Modules.register();
		if (!ModEnvironment.isProduction) {
			ScriptingRuntimeSmokeTest.run();
		}

		Events.RegisterKeyBindings.trigger(KeyBindingHelper::registerKeyBinding);
	}
}