package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.zergatul.cheatutils.sound.SoundLibrary;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundBufferLibrary.class)
public abstract class MixinSoundBufferLibrary {

    @Unique
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> dynamicSoundsMap = new HashMap<>();

    @Inject(at = @At("HEAD"), method = "getCompleteBuffer", cancellable = true)
    private void onGetCompleteBuffer(ResourceLocation location, CallbackInfoReturnable<CompletableFuture<SoundBuffer>> info) {
        if (location.getNamespace().equals("cheatutils") && location.getPath().startsWith("sounds/dynamic/")) {
            var future = dynamicSoundsMap.get(location);
            if (future != null) {
                info.setReturnValue(future);
                return;
            }

            String filename = SoundLibrary.getFileFromLocation(location);
            if (filename == null) {
                return; // cause error in original method?
            }

            File file = new File(filename);
            try (
                InputStream stream = new FileInputStream(file);
                OggAudioStream audioStream = new OggAudioStream(stream);
            ) {
                ByteBuffer byteBuffer = audioStream.readAll();
                SoundBuffer soundBuffer = new SoundBuffer(byteBuffer, audioStream.getFormat());
                future = CompletableFuture.completedFuture(soundBuffer);
                dynamicSoundsMap.put(location, future);
                info.setReturnValue(future);
            } catch (IOException e) {
                SoundLibrary.setLastError(e.getMessage());
                return;
            }
        }
    }
}