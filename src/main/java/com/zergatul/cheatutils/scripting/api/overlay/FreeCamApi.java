package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public class FreeCamApi {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public boolean isEnabled() {
        return FreeCamController.instance.isActive();
    }

    public String getCoordinates() {
        FreeCamController fc = FreeCamController.instance;
        if (fc.isActive()) {
            return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", fc.getX(), fc.getY(), fc.getZ());
        } else {
            return "";
        }
    }

    public String getTargetBlockCoordinates() {
        if (mc.world == null || !FreeCamController.instance.isActive()) {
            return "";
        }

        HitResult hitResult = FreeCamController.instance.getHitResult();
        if (hitResult == null) {
            return "";
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        } else {
            return "";
        }
    }

    public String getTargetBlockName() {
        if (mc.world == null || !FreeCamController.instance.isActive()) {
            return "";
        }

        HitResult hitResult = FreeCamController.instance.getHitResult();
        if (hitResult == null) {
            return "";
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = mc.world.getBlockState(blockPos);
            return ModApiWrapper.BLOCKS.getKey(blockState.getBlock()).toString();
        } else {
            return "";
        }
    }

    private FreeCamConfig getConfig() {
        return ConfigStore.instance.getConfig().freeCamConfig;
    }
}