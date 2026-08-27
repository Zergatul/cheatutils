package com.zergatul.cheatutils.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;

@NullMarked
public class ExternalFileSoundInstance implements SoundInstance {

    private static int COUNTER = 1;

    private final Identifier location;
    private final Sound sound;
    private final WeighedSoundEvents weighted;
    private float volume = 1;

    private ExternalFileSoundInstance() {
        this.location = Identifier.fromNamespaceAndPath("cheatutils", "dynamic/" + (COUNTER++));
        this.sound = new Sound(
                location,
                ConstantFloat.of(1),
                ConstantFloat.of(1),
                1,
                Sound.Type.FILE,
                false,
                false,
                16);
        this.weighted = new WeighedSoundEvents(location, null);
    }

    public static ExternalFileSoundInstance fromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IllegalStateException("File " + filename + " doesn't exist.");
        }

        return new ExternalFileSoundInstance();
    }

    public void setVolume(float value) {
        volume = value;
    }

    @Override
    public Identifier getIdentifier() {
        return location;
    }

    @Override
    public @Nullable WeighedSoundEvents getOrResolve(SoundManager soundManager) {
        return weighted;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public @Nullable WeighedSoundEvents getSoundEvent() {
        return null;
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