package com.zergatul.cheatutils.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SoundLibrary {

    private static final Map<String, ExternalFileSoundInstance> sounds = new HashMap<>();
    private static final Map<ResourceLocation, String> fileMap = new HashMap<>();
    private static String lastError;

    public static ExternalFileSoundInstance get(String filename) {
        ExternalFileSoundInstance instance = sounds.get(filename);
        if (instance != null) {
            return instance;
        }

        try {
            instance = ExternalFileSoundInstance.fromFile(filename);
            sounds.put(filename, instance);
            fileMap.put(instance.getLocation(), filename);
            fileMap.put(Sound.SOUND_LISTER.idToFile(instance.getLocation()), filename);
            return instance;
        } catch (Throwable e) {
            lastError = e.getMessage();
            return null;
        }
    }

    public static String getFileFromLocation(ResourceLocation location) {
        return fileMap.get(location);
    }

    public static String getLastError() {
        return lastError;
    }

    public static void setLastError(String error) {
        lastError = error;
    }
}