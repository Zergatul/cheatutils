package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.SpeedCounterController;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class MainApi {

    private final Minecraft mc = Minecraft.getInstance();

    public boolean isEspEnabled() {
        return ConfigStore.instance.getConfig().esp;
    }

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

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

    @ApiVisibility(ApiType.ACTION)
    public void openClosestChestBoat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            Stream<ChestBoat> boats = StreamSupport
                    .stream(mc.level.entitiesForRendering().spliterator(), false)
                    .filter(e -> e instanceof ChestBoat)
                    .map(e -> (ChestBoat) e);
            double minDistance = Double.MAX_VALUE;
            ChestBoat target = null;
            for (ChestBoat boat: boats.toList()) {
                double d2 = mc.player.distanceToSqr(boat);
                if (d2 < minDistance) {
                    minDistance = d2;
                    target = boat;
                }
            }

            if (target == null) {
                return;
            }

            boolean oldShiftKeyDown = mc.player.input.shiftKeyDown;
            mc.player.input.shiftKeyDown = true;
            mc.gameMode.interactAt(mc.player, target, new EntityHitResult(target), InteractionHand.MAIN_HAND);
            mc.gameMode.interact(mc.player, target, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.input.shiftKeyDown = oldShiftKeyDown;
        }
    }

    @ApiVisibility(ApiType.ACTION)
    public void openTradingWithClosestVillager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            Villager villager = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                    .filter(e -> Villager.class.isInstance(e))
                    .map(e -> (Villager) e)
                    .min(Comparator.comparingDouble(v -> mc.player.distanceToSqr(v)))
                    .orElse(null);
            if (villager == null) {
                return;
            }
            interactWithEntity(mc, villager);
        }
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String text) {
        addText("#FFFFFF", text);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlay.instance.addText(component);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlay.instance.addText(component1.append(" ").append(component2));
    }

    @HelpText("Parameters array should have length dividable by 2, and look like this: [color1, text1, color2, text2] and so on. No space will be added in between, unlike with other addText methods.")
    @ApiVisibility(ApiType.OVERLAY)
    public void addText(String backgroundColor, String[] parameters) {
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addText(background, constructMessage(parameters));
        } else {
            StatusOverlay.instance.addText(constructMessage(parameters));
        }
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addTextAtPosition(int x, int y, String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlay.instance.addFreeText(x, y, component);
    }

    @ApiVisibility(ApiType.OVERLAY)
    public void addTextAtPosition(int x, int y, String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlay.instance.addFreeText(x, y, component1.append(" ").append(component2));
    }

    @HelpText("Parameters array should have length dividable by 2, and look like this: [color1, text1, color2, text2] and so on. No space will be added in between, unlike with other addText methods.")
    @ApiVisibility(ApiType.OVERLAY)
    public void addTextAtPosition(int x, int y, String backgroundColor, String[] parameters) {
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addFreeText(x, y, background, constructMessage(parameters));
        } else {
            StatusOverlay.instance.addFreeText(x, y, constructMessage(parameters));
        }
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
        return mc.gui.getDebugOverlay().showDebugScreen();
    }

    @HelpText("Allowed values: \"left\", \"center\", \"right\".")
    @ApiVisibility(ApiType.OVERLAY)
    public void setOverlayHorizontalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "left" -> StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.LEFT);
            case "center" -> StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.CENTER);
            default -> StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.RIGHT);
        }
    }

    @HelpText("Allowed values: \"top\", \"middle\", \"bottom\".")
    @ApiVisibility(ApiType.OVERLAY)
    public void setOverlayVerticalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "top" -> StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.TOP);
            case "middle" -> StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.MIDDLE);
            default -> StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.BOTTOM);
        }
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

    private void interactWithEntity(Minecraft mc, Entity entity) {
        mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private MutableComponent constructMessage(String[] parameters) {
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return constructMessage("");
        } else {
            MutableComponent component = constructMessage("");
            for (int i = 0; i < parameters.length; i += 2) {
                component = component.append(constructMessage(parameters[i], parameters[i + 1]));
            }
            return component;
        }
    }

    private MutableComponent constructMessage(String text) {
        return MutableComponent.create(new PlainTextContents.LiteralContents(text));
    }

    private MutableComponent constructMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        return component;
    }

    private MutableComponent constructMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        return component1.append(" ").append(component2);
    }

    private void showMessage(MutableComponent message, boolean overlay) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(message, overlay);
    }

    /*public String getSpeed2() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (mc.level != null && server != null) {
            ServerPlayer player = null;
            for (Entity e : server.overworld().getEntities().getAll()) {
                if (e instanceof ServerPlayer p) {
                    player = p;
                    break;
                }
            }
            if (player == null) {
                return "";
            }
            return "Client: " + Root.convert.toString(mc.player.getDeltaMovement().length(), 1) + " Server: " + Root.convert.toString(player.getDeltaMovement().length(), 1);
        } else {
            return "";
        }
    }*/
}