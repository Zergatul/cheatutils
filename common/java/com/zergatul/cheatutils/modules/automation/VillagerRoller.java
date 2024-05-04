package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.mixins.common.accessors.MultiPlayerGameModeAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.BlockUtils;
import com.zergatul.cheatutils.utils.EntityInteraction;
import com.zergatul.cheatutils.wrappers.PickRange;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;

public class VillagerRoller implements Module {

    public static final VillagerRoller instance = new VillagerRoller();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean active;
    private State state = State.STOPPED;
    private volatile BlockPos pos;
    private volatile boolean lecternPlaced;
    private volatile boolean lecternDestroyed;
    private volatile MerchantOffers offers;
    private int villagerId;
    private int slot;
    private String stopReason;
    private String enchantmentId;
    private String enchantmentName;
    private int level;
    private int price;
    private boolean curse;
    private int maxLevel;
    private int minPrice;
    private int maxPrice;
    private Runnable script;

    private VillagerRoller() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
        Events.ScannerBlockUpdated.add(this::onBlockChanged);
        Events.EntityInteract.add(this::onEntityInteract);
        Events.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        if (!active) {
            active = true;
            state = State.SETUP_WAITING_FOR_LECTERN_PLACE;
            pos = null;
            lecternPlaced = false;
            lecternDestroyed = false;
            offers = null;
            stopReason = null;
        }
    }

    public void stop() {
        stop("Requested by user");
    }

    public void toggle() {
        if (active) {
            stop();
        } else {
            start();
        }
    }

    public String getStopReason() {
        return stopReason;
    }

    public void resetStopReason() {
        stopReason = null;
    }

    public String getState() {
        return state.toString();
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    public boolean isBreakingBlock() {
        return active && state == State.BREAKING_LECTERN_PROGRESS;
    }

    public String getEnchantmentId() {
        return enchantmentId;
    }

    public String getEnchantmentName() {
        return enchantmentName;
    }

    public int getLevel() {
        return level;
    }

    public int getPrice() {
        return price;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public boolean isCurse() {
        return curse;
    }

    private void onClientTickEnd() {
        if (!active) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        if (mc.gameMode == null) {
            return;
        }

        while (true) {
            switch (state) {
                case STOPPED, SETUP_WAITING_FOR_VILLAGER_INTERACT -> {
                    return;
                }

                case SETUP_WAITING_FOR_LECTERN_PLACE -> {
                    if (lecternPlaced) {
                        lecternPlaced = false;
                        state = State.SETUP_WAITING_FOR_VILLAGER_INTERACT;
                    }
                    return;
                }

                case SETUP_WAITING_FOR_LECTERN_BREAK -> {
                    if (lecternDestroyed) {
                        lecternDestroyed = false;
                        slot = mc.player.getInventory().selected;
                        state = State.WAITING_FOR_PROFESSION_LOSE;
                    }
                    return;
                }

                case WAITING_FOR_PROFESSION_LOSE -> {
                    mc.gameMode.stopDestroyBlock();
                    Entity entity = mc.level.getEntity(villagerId);
                    if (entity == null) {
                        stop("Selected villager no longer exists");
                        return;
                    }
                    if (entity instanceof Villager villager) {
                        if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                            state = State.PLACING_LECTERN;
                        } else {
                            return;
                        }
                    } else {
                        stop("Selected villager is not a villager anymore. LOL");
                        return;
                    }
                }

                case PLACING_LECTERN -> {
                    Inventory inventory = mc.player.getInventory();
                    int lecternSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        if (inventory.getItem(i).is(Items.LECTERN)) {
                            lecternSlot = i;
                            break;
                        }
                    }

                    if (lecternSlot >= 0) {
                        double reachDistance = PickRange.get();
                        if (pos.distToCenterSqr(mc.player.getEyePosition()) > reachDistance * reachDistance) {
                            // player too far
                            return;
                        }

                        if (!mc.level.getBlockState(pos).canBeReplaced()) {
                            // another block placed at destination
                            return;
                        }

                        CollisionContext collisioncontext = CollisionContext.of(mc.player);
                        if (!mc.level.isUnobstructed(Blocks.LECTERN.defaultBlockState(), pos, collisioncontext)) {
                            // collide with entity
                            return;
                        }

                        inventory.selected = lecternSlot;
                        var plan = new BlockUtils.PlaceBlockPlan(pos.relative(Direction.DOWN).immutable(), Direction.DOWN, pos);
                        BlockUtils.applyPlacingPlan(plan, false);

                        state = State.WAITING_FOR_LECTERN_BLOCK_UPDATE;
                    }
                    return;
                }

                case WAITING_FOR_LECTERN_BLOCK_UPDATE -> {
                    if (lecternPlaced) {
                        lecternPlaced = false;
                        state = State.WAITING_FOR_PROFESSION_GAIN;
                    }
                    return;
                }

                case WAITING_FOR_PROFESSION_GAIN -> {
                    Entity entity = mc.level.getEntity(villagerId);
                    if (entity == null) {
                        stop("Selected villager no longer exists");
                        return;
                    }
                    if (entity instanceof Villager villager) {
                        if (villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN) {
                            EntityInteraction.interact(villager);
                            offers = null;
                            state = State.WAITING_FOR_TRADE_MENU;
                        }
                    } else {
                        stop("Selected villager is not a villager anymore. LOL");
                    }
                    return;
                }

                case WAITING_FOR_TRADE_MENU -> {
                    if (offers == null) {
                        return;
                    }

                    MerchantOffer offer = this.offers.stream()
                            .filter(o -> o.getResult().is(Items.ENCHANTED_BOOK))
                            .findFirst()
                            .orElse(null);
                    this.offers = null;

                    if (offer == null) {
                        // no enchanted book in trades
                        state = State.START_BREAKING_LECTERN;
                        continue;
                    }

                    ItemEnchantments enchantments = offer.getResult().get(DataComponents.STORED_ENCHANTMENTS);
                    if (enchantments == null || enchantments.isEmpty()) {
                        stop("EnchantedBook with 0 enchantments");
                        return;
                    }

                    Enchantment enchantment = enchantments.keySet().stream().findFirst().get().value();
                    this.enchantmentId = Registries.ENCHANTMENTS.getKey(enchantment).toString();
                    this.enchantmentName = Language.getInstance().getOrDefault(enchantment.getDescriptionId());
                    this.level = enchantments.getLevel(enchantment);
                    this.price = offer.getBaseCostA().getCount();

                    this.maxLevel = enchantment.getMaxLevel();
                    this.curse = enchantment.isCurse();
                    this.minPrice = 2 + this.level * 3;
                    this.maxPrice = 6 + this.level * 13;
                    if (enchantment.isTreasureOnly()) {
                        this.minPrice *= 2;
                        this.maxPrice *= 2;
                    }
                    this.minPrice = Math.min(this.minPrice, 64);
                    this.maxPrice = Math.min(this.maxPrice, 64);

                    Runnable script = this.script;
                    if (script != null) {
                        script.run();
                    }

                    if (!active) {
                        return;
                    }

                    state = State.START_BREAKING_LECTERN;
                }

                case START_BREAKING_LECTERN -> {
                    if (mc.level.getBlockState(pos).is(Blocks.LECTERN)) {
                        mc.player.getInventory().selected = slot;
                        if (mc.gameMode.startDestroyBlock(pos, Direction.UP)) {
                            state = State.BREAKING_LECTERN_PROGRESS;
                        }
                    }
                    return;
                }

                case BREAKING_LECTERN_PROGRESS -> {
                    MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
                    if (mode.getIsDestroying_CU() && mode.getDestroyBlockPos_CU().equals(pos) && mode.getDestroyProgress_CU() < 1) {
                        mc.gameMode.continueDestroyBlock(pos, Direction.UP);
                    } else {
                        mc.gameMode.stopDestroyBlock();
                        state = State.WAITING_FOR_LECTERN_BREAK;
                    }
                    return;
                }

                case WAITING_FOR_LECTERN_BREAK -> {
                    if (lecternDestroyed) {
                        lecternDestroyed = false;
                        state = State.WAITING_FOR_PROFESSION_LOSE;
                    }
                    return;
                }
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (!active) {
            return;
        }

        if (args.packet instanceof ClientboundOpenScreenPacket packet) {
            if (packet.getType() == MenuType.MERCHANT) {
                args.skip = true;
            }
        }

        if (args.packet instanceof ClientboundMerchantOffersPacket packet) {
            args.skip = true;
            offers = packet.getOffers();
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClosePacket(packet.getContainerId()));
        }
    }

    private void onBlockChanged(BlockUpdateEvent event) {
        if (active) {
            if (state == State.SETUP_WAITING_FOR_LECTERN_PLACE && event.state().getBlock() == Blocks.LECTERN) {
                pos = event.pos();
                lecternPlaced = true;
                return;
            }

            if (event.pos().equals(pos)) {
                if (event.state().isAir()) {
                    lecternDestroyed = true;
                }
                if (event.state().getBlock() == Blocks.LECTERN) {
                    lecternPlaced = true;
                }
            }
        }
    }

    private void onEntityInteract(Entity entity) {
        if (active && state == State.SETUP_WAITING_FOR_VILLAGER_INTERACT) {
            if (entity instanceof Villager) {
                villagerId = entity.getId();
                state = State.SETUP_WAITING_FOR_LECTERN_BREAK;
            }
        }
    }

    private void onPlayerLoggingOut() {
        stop("Logout");
    }

    private void stop(String reason) {
        if (active) {
            stopReason = reason;
            state = State.STOPPED;
            active = false;
        }
    }

    private enum State {
        STOPPED,
        SETUP_WAITING_FOR_LECTERN_PLACE,
        SETUP_WAITING_FOR_VILLAGER_INTERACT,
        SETUP_WAITING_FOR_LECTERN_BREAK,
        WAITING_FOR_PROFESSION_LOSE,
        PLACING_LECTERN,
        WAITING_FOR_LECTERN_BLOCK_UPDATE,
        WAITING_FOR_PROFESSION_GAIN,
        WAITING_FOR_TRADE_MENU,
        START_BREAKING_LECTERN,
        BREAKING_LECTERN_PROGRESS,
        WAITING_FOR_LECTERN_BREAK
    }
}