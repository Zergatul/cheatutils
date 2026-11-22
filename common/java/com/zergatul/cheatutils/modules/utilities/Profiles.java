package com.zergatul.cheatutils.modules.utilities;

import com.google.gson.*;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ConfigWriterQueue;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.*;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Profiles implements Module {

    public static final Profiles INSTANCE = new Profiles();

    private static final String OLD_CONFIG_FILE_NAME = "zergatulcheatutils.json";
    private static final String PROFILE_CONFIG_FILE_NAME = "cheatutils-profile.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "cheatutils.json";
    private static final String CONFIG_FILE_NAME = "cheatutils.%s.json";

    private final Logger logger = LogManager.getLogger(Profiles.class);
    private final char[] invalidChars = new char[] { '\\', '/', ':', '*', '?', '"', '<', '>', '|' };
    private final Pattern pattern = Pattern.compile("^cheatutils\\.(.+)\\.json$");
    private volatile String current; // changed on the client thread

    private Profiles() {
        WebApiRegistry.INSTANCE.register(new WebApi());

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

        ConfigStore.INSTANCE.loadInitial(profileFile);
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

    public CompletableFuture<Void> createNew(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        return runProfileOperation(() -> {
            current = name;
            requestWrite();
            ConfigStore.INSTANCE.createNew(getProfileFile(name));
        });
    }

    public CompletableFuture<Void> createCopy(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        return runProfileOperation(() -> {
            current = name;
            requestWrite();
            ConfigStore.INSTANCE.switchFile(getProfileFile(name));
        });
    }

    public CompletableFuture<Void> change(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        return runProfileOperation(() -> {
            // the pending write of the previous profile keeps its own file and deadline
            current = name;
            requestWrite();
            ConfigStore.INSTANCE.load(getProfileFile(name));
        });
    }

    public CompletableFuture<Void> delete(String name) {
        if (!isValidProfileName(name)) {
            throw new IllegalStateException("Profile name is not valid.");
        }

        if (name.isEmpty()) {
            throw new IllegalStateException("Cannot delete default profile.");
        }

        return runProfileOperation(() -> {
            current = "";
            requestWrite();
            ConfigStore.INSTANCE.load(getProfileFile(""));

            // after the switch: no new write can target the deleted file, and its pending write is dropped
            File file = getProfileFile(name);
            if (!ConfigWriterQueue.INSTANCE.delete(file)) {
                logger.error("Cannot delete profile file {}", file.getName());
            }
        });
    }

    public String[] reset() {
        // nothing is saved after this, so deleted files stay deleted
        ConfigWriterQueue.INSTANCE.disable();

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

    /**
     * A profile operation replaces every script runtime, so it runs as one step on the client thread at the end of
     * the tick: never inline from inside a script callback. Callers never wait here.
     * Web requests wait on the returned future.
     */
    private CompletableFuture<Void> runProfileOperation(Runnable operation) {
        return ClientThread.post(operation);
    }

    private void requestWrite() {
        String current = this.current;
        File file = getProfileConfigFile();
        ConfigWriterQueue.INSTANCE.queue(file, () -> {
            logger.debug("Saving profile config file {}", file.getName());
            try {
                ConfigWriterQueue.writeAtomically(file, new GsonBuilder().create().toJson(new ProfileConfig(current)));
            } catch (IOException | RuntimeException e) {
                logger.error("Cannot write profile config", e);
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

    private final class WebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "profiles";
        }

        @Override
        public String get(String command) {
            return switch (command) {
                case "current" -> gson.toJson(Profiles.this.getCurrent());
                case "list" -> gson.toJson(Profiles.this.list());
                default -> throw new ApiException("Unsupported command.", HttpResponseCodes.BAD_REQUEST);
            };
        }

        @Override
        public String post(String body) {
            Request request = gson.fromJson(body, Request.class);
            CompletableFuture<Void> operation = switch (request.command) {
                case "change" -> Profiles.this.change(request.name);
                case "copy" -> Profiles.this.createCopy(request.name);
                case "new" -> Profiles.this.createNew(request.name);
                default -> throw new ApiException("Unsupported command.", HttpResponseCodes.BAD_REQUEST);
            };

            // reply when the new profile is active, so the page reloads its settings
            ClientThread.await(operation);
            return "{}";
        }

        @Override
        public String delete(String name) {
            ClientThread.await(Profiles.this.delete(name));
            return "{}";
        }

        public record Request(String command, String name) {}
    }
}