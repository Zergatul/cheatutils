package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Profiles implements Module {

    public static final Profiles instance = new Profiles();

    private static final String OLD_CONFIG_FILE_NAME = "zergatulcheatutils.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "cheatutils.json";

    private final Logger logger = LogManager.getLogger(Profiles.class);

    private Profiles() {}

    public void init() {
        migration1();
        ConfigStore.instance.read(getProfileFile());
    }

    private File getProfileFile() {
        return new File(getConfigDirectory(), DEFAULT_CONFIG_FILE_NAME);
    }

    private File getConfigDirectory() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                logger.error("Cannot create config directory");
            }
        }
        return configDir;
    }

    private void migration1() {
        File old = new File(getConfigDirectory(), OLD_CONFIG_FILE_NAME);
        if (old.exists()) {
            if (!old.renameTo(getProfileFile())) {
                logger.error("Cannot rename old config file");
            }
        }
    }
}
