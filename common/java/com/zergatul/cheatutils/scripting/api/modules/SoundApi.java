package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.sound.ExternalFileSoundInstance;
import com.zergatul.cheatutils.sound.SoundLibrary;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.Minecraft;

public class SoundApi {

    @HelpText("Only .ogg files are supported")
    public boolean play(String filename) {
        return play(filename, 1);
    }

    @HelpText("Only .ogg files are supported")
    public boolean play(String filename, double volume) {
        ExternalFileSoundInstance instance = SoundLibrary.get(filename);
        if (instance == null) {
            return false;
        }

        instance.setVolume((float) MathUtils.clamp(volume, 0, 1));
        Minecraft.getInstance().getSoundManager().play(instance);
        return true;
    }

    @HelpText("When sound.play(...) returns false, you can get error text by calling this method")
    public String getLastError() {
        String error = SoundLibrary.getLastError();
        return error == null ? "" : error;
    }
}