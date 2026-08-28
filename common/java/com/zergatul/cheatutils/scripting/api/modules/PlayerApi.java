package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.controllers.DisconnectController;
import com.zergatul.cheatutils.controllers.SpeedCounterController;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class PlayerApi {

    private final static Minecraft mc = Minecraft.getInstance();

    public final TargetApi target = new TargetApi();

    @ApiVisibility(ApiType.ACTION)
    public void chat(String text) {
        LocalPlayer player = mc.player;
        if (player != null) {
            player.connection.sendChat(text);
        }
    }

    @MethodDescription("Sends an ingame command. The first character must be slash.")
    @ApiVisibility(ApiType.ACTION)
    public void command(String text) {
        if (text != null && text.startsWith("/")) {
            LocalPlayer player = mc.player;
            if (player != null) {
                player.connection.sendCommand(text.substring(1));
            }
        }
    }

    public String getCoordinatesFormatted() {
        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", entity.getX(), entity.getY(), entity.getZ());
    }

    @MethodDescription("If you are in the Overworld, returns calculated coordinates in the Nether")
    public String getCalculatedNetherCoordinates() {
        Entity entity = mc.getCameraEntity();
        if (mc.level == null || mc.level.dimension() == Level.NETHER || entity == null) {
            return "";
        }
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", entity.getX() / 8, entity.getY(), entity.getZ() / 8);
    }

    @MethodDescription("If you are in the Nether, returns calculated coordinates in the Overworld")
    public String getCalculatedOverworldCoordinates() {
        Entity entity = mc.getCameraEntity();
        if (mc.level == null || mc.level.dimension() == Level.OVERWORLD || entity == null) {
            return "";
        }
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", entity.getX() * 8, entity.getY(), entity.getZ() * 8);
    }

    public String getBlockCoordinatesFormatted() {
        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }
        BlockPos pos = entity.blockPosition();
        return String.format(Locale.ROOT, "%d %d %d [%d %d]", pos.getX(), pos.getY(), pos.getZ(), pos.getX() & 15, pos.getZ() & 15);
    }

    public String getChunkCoordinatesFormatted() {
        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }
        ChunkPos pos = new ChunkPos(entity.blockPosition());
        return String.format(Locale.ROOT, "%d %d", pos.x, pos.z);
    }

    public String getDirection() {
        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }
        Direction direction = entity.getDirection();
        return direction.getName();
    }

    public String getBiome() {
        Entity entity = mc.getCameraEntity();
        if (mc.level == null || entity == null) {
            return "";
        }
        Holder<Biome> holder = mc.level.getBiome(entity.blockPosition());
        return holder.unwrap().map(id -> id.location().toString(), biome -> "[unregistered " + biome + "]");
    }

    @MethodDescription("Measured in 0.5 sec window.")
    public String getHorizontalSpeed() {
        return String.format(Locale.ROOT, "%.3f", SpeedCounterController.instance.getHorizontalSpeed());
    }

    @MethodDescription("Measured in 0.5 sec window.")
    public String getSpeed() {
        return String.format(Locale.ROOT, "%.3f", SpeedCounterController.instance.getSpeed());
    }

    public double getX() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getX();
    }

    public double getY() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getY();
    }

    public double getZ() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getZ();
    }

    public double getXRot() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getXRot();
    }

    public double getYRot() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getYRot();
    }

    @ApiVisibility(ApiType.ACTION)
    public void setXRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setXRot((float)value);
    }

    @ApiVisibility(ApiType.ACTION)
    public void setYRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setYRot((float)value);
    }

    public int getHealth() {
        if (mc.player == null) {
            return 0;
        }

        return (int) mc.player.getHealth();
    }

    public int getFood() {
        if (mc.player == null) {
            return 0;
        }

        return mc.player.getFoodData().getFoodLevel();
    }

    public boolean isUnderwater() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isUnderWater();
    }

    public boolean isElytraFlying() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isFallFlying();
    }

    public boolean isOnGround() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.onGround();
    }

    public boolean isPassenger() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isPassenger();
    }

    @ApiVisibility(ApiType.ACTION)
    public void lookAt(double x, double y, double z) {
        if (mc.player == null) {
            return;
        }
        Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), new Vec3(x, y, z));
        mc.player.setXRot(rotation.xRot());
        mc.player.setYRot(rotation.yRot());
    }

    @MethodDescription("""
            Allowed disconnect types: "self-attack", "invalid-chars". Anything else (for example "") - normal disconnect.
            """)
    @HelpText("Allowed types: \"self-attack\", \"invalid-chars\"; anything else performs a normal disconnect.")
    @ApiVisibility(ApiType.ACTION)
    public void disconnect(String type) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(null);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(null);
            default -> DisconnectController.instance.disconnect(null);
        }
    }

    @MethodDescription("""
            Allowed disconnect types: "self-attack", "invalid-chars". Anything else (for example "") - normal disconnect.
            You can specify a custom message to be displayed on the disconnect screen.
            """)
    @HelpText("The second argument is displayed on the disconnect screen.")
    @ApiVisibility(ApiType.ACTION)
    public void disconnect(String type, String message) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(message);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(message);
            default -> DisconnectController.instance.disconnect(message);
        }
    }

    public static class TargetApi {

        public String getBlockCoordinatesFormatted() {
            BlockHitResult result = getBlockHitResult();
            if (result == null) {
                return "";
            }
            BlockPos pos = result.getBlockPos();
            return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        }

        public String getBlockName() {
            BlockHitResult result = getBlockHitResult();
            if (result == null || mc.level == null) {
                return "";
            }
            BlockState state = mc.level.getBlockState(result.getBlockPos());
            return Registries.BLOCKS.getKey(state.getBlock()).toString();
        }

        private BlockHitResult getBlockHitResult() {
            Entity entity = mc.getCameraEntity();
            if (mc.level == null || entity == null) {
                return null;
            }
            HitResult result = entity.pick(20, 0, false);
            return result instanceof BlockHitResult block && result.getType() == HitResult.Type.BLOCK ? block : null;
        }
    }
}