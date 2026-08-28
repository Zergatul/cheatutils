package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.SpeedCounterController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.scripting.api.Root;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MainApi {

    private final Minecraft mc = Minecraft.getInstance();

    @ApiVisibility(ApiType.ACTION)
    public void chat(String text) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.sendChat(text);
        }
    }

    @HelpText("for server commands, like /home")
    @ApiVisibility(ApiType.ACTION)
    public void command(String text) {
        if (text != null && text.startsWith("/")) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.connection.sendCommand(text.substring(1));
            }
        }
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String text) {
        showMessage(constructMessage(text), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String text) {
        showMessage(constructMessage(text), true);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String color, String text) {
        showMessage(constructMessage(color, text), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String color, String text) {
        showMessage(constructMessage(color, text), true);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(color1, text1, color2, text2), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(color1, text1, color2, text2), true);
    }

    public String getCoordinates() {
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX(), mc.getCameraEntity().getY(), mc.getCameraEntity().getZ());
    }

    @HelpText("If you are in the Overworld, returns calculated coordinates in the Nether")
    public String getCalcNetherCoordinates() {
        if (mc.level == null || mc.level.dimension() == Level.NETHER) {
            return "";
        }
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX() / 8, mc.getCameraEntity().getY(), mc.getCameraEntity().getZ() / 8);
    }

    @HelpText("If you are in the Nether, returns calculated coordinates in the Overworld")
    public String getCalcOverworldCoordinates() {
        if (mc.level == null || mc.level.dimension() == Level.OVERWORLD) {
            return "";
        }
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX() * 8, mc.getCameraEntity().getY(), mc.getCameraEntity().getZ() * 8);
    }

    public boolean isDebugScreenEnabled() {
        return mc.options.renderDebug;
    }

    public String getTargetBlockCoordinates() {
        if (mc.level == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.pick(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        } else {
            return "";
        }
    }

    public String getTargetBlockName() {
        if (mc.level == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.pick(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            BlockState blockState = mc.level.getBlockState(blockPos);
            return Registries.BLOCKS.getKey(blockState.getBlock()).toString();
        } else {
            return "";
        }
    }

    public String getBlockCoordinates() {
        BlockPos blockPos = mc.getCameraEntity().blockPosition();
        return String.format(Locale.ROOT, "%d %d %d [%d %d]",
                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                blockPos.getX() & 15, blockPos.getZ() & 15);
    }

    public String getChunkCoordinates() {
        BlockPos blockPos = mc.getCameraEntity().blockPosition();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        return String.format(Locale.ROOT, "%d %d", chunkPos.x, chunkPos.z);
    }

    public String getDirection() {
        Direction direction = mc.getCameraEntity().getDirection();
        return direction.getName();
    }

    public String getBiome() {
        if (mc.level == null || mc.getCameraEntity() == null) {
            return "";
        }
        BlockPos blockPos = mc.getCameraEntity().blockPosition();
        Holder<Biome> holder = mc.level.getBiome(blockPos);
        return holder.unwrap().map(id -> id.location().toString(), biome -> "[unregistered " + biome + "]");
    }

    @HelpText("Measured in 0.5 sec window.")
    public String getHorizontalSpeed() {
        return String.format(Locale.ROOT, "%.3f", SpeedCounterController.instance.getHorizontalSpeed());
    }

    @HelpText("Measured in 0.5 sec window.")
    public String getSpeed() {
        return String.format(Locale.ROOT, "%.3f", SpeedCounterController.instance.getSpeed());
    }

    private MutableComponent constructMessage(String text) {
        return MutableComponent.create(new LiteralContents(text));
    }

    private MutableComponent constructMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        return component;
    }

    private MutableComponent constructMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        return component1.append(" ").append(component2);
    }

    private void showMessage(MutableComponent message, boolean overlay) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(message, overlay);
    }
}