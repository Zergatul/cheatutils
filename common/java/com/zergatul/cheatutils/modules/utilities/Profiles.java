package com.zergatul.cheatutils.modules.utilities;

import com.google.gson.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ConfigWriterQueue;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Profiles implements Module {

    public static final Profiles instance = new Profiles();

    private static final long WRITE_FILE_DELAY = 15 * 1_000_000_000L;
    private static final String OLD_CONFIG_FILE_NAME = "zergatulcheatutils.json";
    private static final String PROFILE_CONFIG_FILE_NAME = "cheatutils-profile.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "cheatutils.json";
    private static final String CONFIG_FILE_NAME = "cheatutils.%s.json";

    private final Logger logger = LogManager.getLogger(Profiles.class);
    private final char[] invalidChars = new char[] { '\\', '/', ':', '*', '?', '"', '<', '>', '|' };
    private final Pattern pattern = Pattern.compile("^cheatutils\\.(.+)\\.json$");
    private String current;
    private volatile boolean isInResetState = false;

    private Profiles() {
        current = "";
    }

    public void init() {
        migration1();

        File profileConfigFile = getProfileConfigFile();
        if (profileConfigFile.exists()) {
            Gson gson = new GsonBuilder().create();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(profileConfigFile));
                ProfileConfig config = gson.fromJson(reader, ProfileConfig.class);
                if (isValidProfileName(config.name)) {
                    current = config.name;
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        File profileFile = getProfileFile(current);
        if (!profileFile.exists()) {
            current = "";
            profileFile = getProfileFile(current);
        }

        ConfigStore.instance.read(profileFile);
    }

    public String getCurrent() {
        return current;
    }

    public List<String> list() {
        List<String> result = new ArrayList<>();
        String[] files = getConfigDirectory().list();
        if (files != null) {
            for (String name : files) {
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    result.add(matcher.group(1));
                }
            }
        }

        if (!current.isEmpty() && !result.contains(current)) {
            // probably not saved yet to file
            result.add(current);
        }

        Collections.sort(result);

        return result;
    }

    public boolean isValidProfileName(String name) {
        if (name == null) {
            return false;
        }

        boolean valid = true;
        for (int i = 0; i < name.length(); i++) {
            char current = name.charAt(i);

            for (char invalidChar : invalidChars) {
                if (current == invalidChar) {
                    valid = false;
                    break;
                }
            }

            if (!valid) {
                break;
            }
        }

        return valid;
    }

    public void createNew(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        current = name;
        requestWrite();

        executeInRenderThread(() -> ConfigStore.instance.createNew(getProfileFile(name)));
    }

    public void createCopy(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        current = name;
        requestWrite();

        executeInRenderThread(() -> ConfigStore.instance.switchFile(getProfileFile(name)));
    }

    public void change(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        current = name;
        requestWrite();

        executeInRenderThread(() -> ConfigStore.instance.read(getProfileFile(name)));
    }

    public void delete(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        if (name.isEmpty()) {
            throw new IllegalStateException("Cannot delete default profile.");
        }

        File file = getProfileFile(name);
        if (file.exists()) {
            if (!file.delete()) {
                logger.error("Cannot delete profile file.");
            }
        }

        current = "";
        requestWrite();

        executeInRenderThread(() -> ConfigStore.instance.read(getProfileFile("")));
    }

    public boolean isInResetState() {
        return isInResetState;
    }

    public String[] reset() {
        isInResetState = true;
        ConfigWriterQueue.instance.clear();

        // remove files
        String[] files = getConfigDirectory().list();
        if (files == null) {
            return new String[] { "Cannot get list of files in config directory" };
        }

        List<String> errors = new ArrayList<>();
        for (String name : files) {
            boolean shouldDelete = false;

            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                shouldDelete = true;
            }

            if (name.equalsIgnoreCase(OLD_CONFIG_FILE_NAME)) {
                shouldDelete = true;
            }

            if (name.equalsIgnoreCase(PROFILE_CONFIG_FILE_NAME)) {
                shouldDelete = true;
            }

            if (name.equalsIgnoreCase(DEFAULT_CONFIG_FILE_NAME)) {
                shouldDelete = true;
            }

            if (shouldDelete) {
                File file = new File(getConfigDirectory(), name);
                if (!file.delete()) {
                    errors.add(String.format("Cannot delete profile file '%s'", file.getAbsolutePath()));
                }
            }
        }

        return errors.toArray(String[]::new);
    }

    private void executeInRenderThread(Runnable runnable) {
        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            TickEndExecutor.instance.execute(runnable);
        }
    }

    private void requestWrite() {
        String current = this.current;
        File file = getProfileConfigFile();
        Gson gson = new GsonBuilder().create();
        ConfigWriterQueue.instance.queue(file, WRITE_FILE_DELAY, () -> {
            logger.debug("Saving profile config file " + file.getName());
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(new ProfileConfig(current), writer);
                writer.close();
            }
            catch (Exception e) {
                logger.error("Cannot write profile config");
                logger.error(e);
            }
        });
    }

    private File getProfileConfigFile() {
        return new File(getConfigDirectory(), PROFILE_CONFIG_FILE_NAME);
    }

    private File getProfileFile(String name) {
        if (name.isEmpty()) {
            return new File(getConfigDirectory(), DEFAULT_CONFIG_FILE_NAME);
        } else {
            return new File(getConfigDirectory(), String.format(CONFIG_FILE_NAME, name));
        }
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
            if (!old.renameTo(getProfileFile(""))) {
                logger.error("Cannot rename old config file");
            }
        }
    }

    public record ProfileConfig(String name) {}
}