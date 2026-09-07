package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ConfigWriterQueue;
import com.zergatul.cheatutils.modules.Module;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Called on the client thread, including all HTTP operations. */
public class Profiles implements Module {

    public static final Profiles instance = new Profiles();

    private static final String PROFILE_CONFIG_FILE_NAME = "cheatutils-profile.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "cheatutils.json";
    private static final Pattern PATTERN = Pattern.compile("^cheatutils\\.(.+)\\.json$");

    private File directory;
    private String current = "";
    private boolean reset;

    public void init(File directory) {
        this.directory = directory;
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalStateException("Cannot create config directory " + directory);
        }
        File selected = getProfileConfigFile();
        if (selected.isFile()) {
            try (Reader reader = Files.newBufferedReader(selected.toPath(), StandardCharsets.UTF_8)) {
                ProfileConfig config = ConfigStore.instance.gson.fromJson(reader, ProfileConfig.class);
                if (config != null && isValidProfileName(config.name)) {
                    current = config.name;
                }
            } catch (IOException | RuntimeException e) {
                LogManager.getLogger(Profiles.class).error("Cannot read selected profile", e);
            }
        }
        if (!getProfileFile(current).isFile()) {
            current = "";
        }
        ConfigStore.instance.read(getProfileFile(current));
        ConfigStore.instance.requestWrite();
    }

    public String getCurrent() {
        return current;
    }

    public List<String> list() {
        List<String> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Matcher matcher = PATTERN.matcher(file.getName());
                if (file.isFile() && matcher.matches() && isValidProfileName(matcher.group(1))) {
                    result.add(matcher.group(1));
                }
            }
        }
        if (!current.isEmpty() && !result.contains(current)) {
            result.add(current);
        }
        Collections.sort(result);
        return result;
    }

    public boolean isValidProfileName(String name) {
        if (name == null || name.length() > 100) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (c < 32 || "\\/:*?\"<>|".indexOf(c) >= 0) {
                return false;
            }
        }
        return name.isEmpty() || (!name.trim().isEmpty() && !name.endsWith(".") && !name.endsWith(" "));
    }

    public void createNew(String name) {
        validateNew(name);
        flushCurrent();
        ConfigStore.instance.createNew(getProfileFile(name));
        select(name);
    }

    public void createCopy(String name) {
        validateNew(name);
        flushCurrent();
        ConfigStore.instance.switchFile(getProfileFile(name));
        select(name);
    }

    public void change(String name) {
        validate(name);
        if (!name.isEmpty() && !name.equals(current) && !getProfileFile(name).isFile()) {
            throw new IllegalArgumentException("Profile does not exist.");
        }
        flushCurrent();
        ConfigStore.instance.read(getProfileFile(name));
        select(name);
    }

    public void delete(String name) {
        validate(name);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete default profile.");
        }
        File file = getProfileFile(name);
        ConfigWriterQueue.instance.cancel(file);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot delete profile.", e);
        }
        if (current.equalsIgnoreCase(name)) {
            ConfigStore.instance.read(getProfileFile(""));
            select("");
        }
    }

    public boolean isInResetState() {
        return reset;
    }

    /** Reset leaves saving disabled until the next client launch, as in 26.2. */
    public String[] reset() {
        reset = true;
        ConfigWriterQueue.instance.clear();
        List<String> errors = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return new String[] { "Cannot list config directory." };
        }
        for (File file : files) {
            String name = file.getName();
            if (file.isFile() && (PATTERN.matcher(name).matches() || name.equals(DEFAULT_CONFIG_FILE_NAME)
                    || name.equals(PROFILE_CONFIG_FILE_NAME))) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    errors.add("Cannot delete " + name);
                }
            }
        }
        return errors.toArray(new String[0]);
    }

    private void select(String name) {
        current = name;
        // Persist the target before remembering its name, including newly created profiles.
        ConfigWriterQueue.instance.flush(getProfileFile(name));
        String json = ConfigStore.instance.gson.toJson(new ProfileConfig(name));
        File file = getProfileConfigFile();
        ConfigWriterQueue.instance.queue(file, ConfigStore.WRITE_FILE_DELAY, () -> ConfigStore.instance.write(file, json));
    }

    private void flushCurrent() {
        ConfigWriterQueue.instance.flush(getProfileFile(current));
    }

    private void validate(String name) {
        if (reset) {
            throw new IllegalStateException("Restart Minecraft after resetting configuration.");
        }
        if (!isValidProfileName(name)) {
            throw new IllegalArgumentException("Profile name is not valid.");
        }
    }

    private void validateNew(String name) {
        validate(name);
        if (name.isEmpty() || list().stream().anyMatch(existing -> existing.equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Profile already exists or name is empty.");
        }
    }

    private File getProfileFile(String name) {
        return new File(directory, name.isEmpty() ? DEFAULT_CONFIG_FILE_NAME : "cheatutils." + name + ".json");
    }

    private File getProfileConfigFile() {
        return new File(directory, PROFILE_CONFIG_FILE_NAME);
    }

    public static class ProfileConfig {

        public String name;

        public ProfileConfig(String name) {
            this.name = name;
        }
    }
}