package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.StatusOverlayController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public class MainApi {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public boolean isEspEnabled() {
        return ConfigStore.instance.getConfig().esp;
    }

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.ACTION)
    public void chat(String text) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            MinecraftClient.getInstance().player.networkHandler.sendChatMessage(text);
        }
    }

    @HelpText("for server commands, like /home")
    @ApiVisibility(ApiType.ACTION)
    public void command(String text) {
        if (text != null && text.startsWith("/")) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                MinecraftClient.getInstance().player.networkHandler.sendCommand(text.substring(1));
            }
        }
    }

    @ApiVisibility(ApiType.ACTION)
    public void systemMessage(String text) {
        showMessage(constructMessage(text), false);
    }

    @ApiVisibility(ApiType.ACTION)
    public void overlayMessage(String text) {
        showMessage(constructMessage(text), true);
    }

    @ApiVisibility(ApiType.ACTION)
    public void systemMessage(String color, String text) {
        showMessage(constructMessage(color, text), false);
    }

    @ApiVisibility(ApiType.ACTION)
    public void overlayMessage(String color, String text) {
        showMessage(constructMessage(color, text), true);
    }

    @ApiVisibility(ApiType.ACTION)
    public void systemMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(color1, text1, color2, text2), false);
    }

    @ApiVisibility(ApiType.ACTION)
    public void overlayMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(color1, text1, color2, text2), true);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String text) {
        addText("#FFFFFF", text);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableText component = MutableText.of(new LiteralTextContent(text));
        if (colorInt != null) {
            component = component.setStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlayController.instance.addText(component);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableText component1 = MutableText.of(new LiteralTextContent(text1));
        if (color1Int != null) {
            component1 = component1.setStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableText component2 = MutableText.of(new LiteralTextContent(text2));
        if (color2Int != null) {
            component2 = component2.setStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlayController.instance.addText(component1.append(" ").append(component2));
    }

    public String getCoordinates() {
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX(), mc.getCameraEntity().getY(), mc.getCameraEntity().getZ());
    }

    public boolean isDebugScreenEnabled() {
        return mc.options.debugEnabled;
    }

    @HelpText("Allowed values: \"left\", \"center\", \"right\".")
    @ApiVisibility(ApiType.OVERLAY)
    public void setOverlayHorizontalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "left" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.LEFT);
            case "center" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.CENTER);
            default -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.RIGHT);
        }
    }

    @HelpText("Allowed values: \"top\", \"middle\", \"bottom\".")
    @ApiVisibility(ApiType.OVERLAY)
    public void setOverlayVerticalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "top" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.TOP);
            case "middle" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.MIDDLE);
            default -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.BOTTOM);
        }
    }

    public String getTargetBlockCoordinates() {
        if (mc.world == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.raycast(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        } else {
            return "";
        }
    }

    public String getTargetBlockName() {
        if (mc.world == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.raycast(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            BlockState blockState = mc.world.getBlockState(blockPos);
            return ModApiWrapper.BLOCKS.getKey(blockState.getBlock()).toString();
        } else {
            return "";
        }
    }

    /*public String getBlockCoordinates() {
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
    }*/

    /*public String getPlayerEntitySeed() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            var rnd = (LegacyRandomSource) players.get(0).getRandom();
            var seed = (AtomicLong) ReflectionUtils.getDeclaredField(rnd, "seed");
            return Long.toHexString(seed.get());
        } else {
            return "";
        }
    }

    public String getEnchantmentSeed() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            var seed = players.get(0).getEnchantmentSeed();
            return Integer.toHexString(seed);
        } else {
            return "";
        }
    }

    public int getEnchantmentSeedInt() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            return players.get(0).getEnchantmentSeed();
        } else {
            return Integer.MIN_VALUE;
        }
    }*/

    /*private void interactWithEntity(Minecraft mc, Entity entity) {
        mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }*/

    private Text constructMessage(String text) {
        return MutableText.of(new LiteralTextContent(text));
    }

    private Text constructMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableText component = MutableText.of(new LiteralTextContent(text));
        if (colorInt != null) {
            component = component.setStyle(Style.EMPTY.withColor(colorInt));
        }
        return component;
    }

    private Text constructMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableText component1 = MutableText.of(new LiteralTextContent(text1));
        if (color1Int != null) {
            component1 = component1.setStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableText component2 = MutableText.of(new LiteralTextContent(text2));
        if (color2Int != null) {
            component2 = component2.setStyle(Style.EMPTY.withColor(color2Int));
        }
        return component1.append(" ").append(component2);
    }

    private void showMessage(Text message, boolean overlay) {
        MinecraftClient.getInstance().getMessageHandler().onGameMessage(message, overlay);
    }
}