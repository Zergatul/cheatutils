package com.zergatul.cheatutils.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;

import java.io.File;

public class ExternalFileSoundInstance implements SoundInstance {

    private static int counter = 1;
    private final String filename;
    private final ResourceLocation location;
    private final Sound sound;
    private float volume = 1;

    private ExternalFileSoundInstance(String filename) {
        this.filename = filename;
        this.location = ResourceLocation.fromNamespaceAndPath("cheatutils", "dynamic/" + (counter++));
        this.sound = new Sound(
                location,
                ConstantFloat.of(1),
                ConstantFloat.of(1),
                1,
                Sound.Type.FILE,
                false,
                false,
                16);
    }

    public static ExternalFileSoundInstance fromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IllegalStateException("File " + filename + " doesn't exist.");
        }

        return new ExternalFileSoundInstance(filename);
    }

    public String getFilename() {
        return filename;
    }

    public void setVolume(float value) {
        volume = value;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        return new WeighedSoundEvents(location, null);
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public SoundSource getSource() {
        return SoundSource.MASTER;
    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return true;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return 1;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public Attenuation getAttenuation() {
        return Attenuation.NONE;
    }
}