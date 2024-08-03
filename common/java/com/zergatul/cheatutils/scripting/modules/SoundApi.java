package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.MethodDescription;
import com.zergatul.cheatutils.sound.ExternalFileSoundInstance;
import com.zergatul.cheatutils.sound.SoundLibrary;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class SoundApi {

    @MethodDescription("Only .ogg files are supported")
    public boolean play(String filename) {
        return play(filename, 1);
    }

    @MethodDescription("Only .ogg files are supported")
    public boolean play(String filename, double volume) {
        ExternalFileSoundInstance instance = SoundLibrary.get(filename);
        if (instance == null) {
            return false;
        }

        instance.setVolume((float) MathUtils.clamp(volume, 0, 1));
        Minecraft.getInstance().getSoundManager().play(instance);
        return true;
    }

    @MethodDescription("When sound.play(...) returns false, you can get error text by calling this method")
    public String getLastError() {
        String error = SoundLibrary.getLastError();
        return error == null ? "" : error;
    }
}