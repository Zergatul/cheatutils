package com.zergatul.cheatutils.modules.esp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTitleConfig;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.entities.EntityLike;
import com.zergatul.cheatutils.font.*;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.modules.visuals.LogoutSpots;
import com.zergatul.cheatutils.ui.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EntityTitle implements FontBackendHolder {

    public static final EntityTitle instance = new EntityTitle();

    private final Minecraft mc = Minecraft.getInstance();
    private final ArrayList<StylizedTextChunk> buffer = new ArrayList<>();
    private final StringBuilder builder = new StringBuilder();

    private final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public Optional<String> load(UUID uuid) {
                    CompletableFuture.runAsync(() -> {
                        ProfileResult result = Minecraft.getInstance().services().sessionService().fetchProfile(uuid, false);
                        if (result == null) {
                            usernameCache.put(uuid, Optional.of(uuid.toString()));
                        } else {
                            usernameCache.put(uuid, Optional.of(result.profile().name()));
                        }
                    });
                    return Optional.of("loading...");
                }
            });

    private final List<EntityEntry> entities = new ArrayList<>();

    private boolean titleFontChanged;
    private CompletableFuture<FontBackend> titleFontBackendFuture;
    private FontRenderer titleFontRenderer;

    private boolean enchantmentFontChanged;
    private CompletableFuture<FontBackend> enchantmentFontBackendFuture;
    private FontRenderer enchantmentFontRenderer;

    private EntityTitle() {
        Events.AfterRenderWorld.add(this::onRenderWorld, 20);
    }

    @Override
    public boolean uses(FontBackend backend) {
        if (titleFontRenderer != null && titleFontRenderer.uses(backend)) {
            return true;
        }
        if (enchantmentFontRenderer != null && enchantmentFontRenderer.uses(backend)) {
            return true;
        }
        return false;
    }

    public void onTitleFontChange() {
        TickEndExecutor.instance.execute(() -> titleFontChanged = true);
    }

    public void onEnchantmentFontChange() {
        TickEndExecutor.instance.execute(() -> enchantmentFontChanged = true);
    }

    private void onRenderWorld(RenderWorldLastEvent event) {
        entities.clear();
        if (mc.level == null) {
            return;
        }

        if (!EntityEsp.instance.isEnabled() || !EspGlobal.enabled) {
            return;
        }

        ImmutableList<EntityEspConfig> entityConfigs = ConfigStore.instance.getConfig().entities.configs;

        Vec3 view = event.getCameraState().pos;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                continue;
            }

            Vec3 pos = entity.getPosition(event.getPartialTickTime());
            double distanceSqr = pos.distanceToSqr(view);

            boolean drawTitles = false;
            boolean showDefaultNames = false;
            boolean showHp = false;
            boolean showEquippedItems = false;
            boolean useRaw = false;
            boolean showOwner = false;
            StylizedText title = null;
            for (EntityEspConfig entityConfig : entityConfigs) {
                if (!entityConfig.enabled || !entityConfig.drawTitles) {
                    continue;
                }
                if (!entityConfig.isValidEntity(entity)) {
                    continue;
                }

                if (distanceSqr < entityConfig.maxDistance * entityConfig.maxDistance) {
                    drawTitles = true;
                    showDefaultNames |= entityConfig.showDefaultNames;
                    useRaw |= entityConfig.useRawNames;
                    showHp |= entityConfig.showHp;
                    showEquippedItems |= entityConfig.showEquippedItems;
                    showOwner |= entityConfig.showOwner;
                    if (title == null) {
                        title = EntityEsp.instance.getTitleOverride(entityConfig, entity);
                    }
                }
            }

            if (drawTitles) {
                pos = pos.add(-view.x, -view.y + entity.getBbHeight(), -view.z);
                entities.add(new EntityEntry(
                        EntityLike.fromEntity(entity),
                        pos,
                        distanceSqr,
                        showDefaultNames,
                        useRaw,
                        showHp,
                        showEquippedItems,
                        showOwner,
                        title,
                        null));
            }
        }

        addDisconnectedPlayers(view);

        entities.sort((e1, e2) -> -Double.compare(e1.distanceSqr, e2.distanceSqr));

        renderEntityTitles(event);
    }

    private void renderEntityTitles(RenderWorldLastEvent event) {
        if (!EspGlobal.enabled) {
            return;
        }

        EntityTitleConfig config = ConfigStore.instance.getConfig().entityTitleConfig;

        if (titleFontChanged) {
            titleFontBackendFuture = FontLibrary.instance.createBackend(config.titleFont.asFontParameters());
            // titleFontRenderer = null; // more smooth transition, but shows prev font for few frames?
            titleFontChanged = false;
        }

        if (enchantmentFontChanged) {
            enchantmentFontBackendFuture = FontLibrary.instance.createBackend(config.enchantmentFont.asFontParameters());
            // enchantmentFontRenderer = null; // more smooth transition, but shows prev font for few frames?
            enchantmentFontChanged = false;
        }

        if (titleFontBackendFuture != null) {
            if (titleFontBackendFuture.isDone()) {
                titleFontRenderer = titleFontBackendFuture.join().createFontRenderer(config.titleFont.asFontRenderDetails());
                titleFontBackendFuture = null;
            }
        }
        if (enchantmentFontBackendFuture != null) {
            if (enchantmentFontBackendFuture.isDone()) {
                enchantmentFontRenderer = enchantmentFontBackendFuture.join().createFontRenderer(config.enchantmentFont.asFontRenderDetails());
                enchantmentFontBackendFuture = null;
            }
        }
        if (titleFontRenderer == null) {
            return;
        }

        int scale = mc.getWindow().getGuiScale();
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

        RenderingContext context = new RenderingContext(event.getGuiGraphics(), matrix, halfScrWidth, halfScrHeight);

        List<ItemStack> items = new ArrayList<>();
        List<List<EnchantmentEntry>> enchantments = new ArrayList<>();

        for (EntityEntry entry : entities) {
            Vector4f v1 = event.getViewRotation().transform(new Vector4f((float)entry.position.x, (float)entry.position.y, (float)entry.position.z, 1));
            Vector4f v2 = event.getProjection().transform(v1);
            if (v2.z <= 0) {
                continue; // behind
            }

            int xc = Math.round(v2.x / v2.w * halfScrWidth);
            int yc = Math.round(-v2.y / v2.w * halfScrHeight);

            StylizedText text = getEntityText(config, entry);
            FlexColumnElement flex = new FlexColumnElement().setGap(context.getScale());

            if (text != null) {
                flex.append(
                        new DivisionElement()
                                .setMargin(context.getScale())
                                .setBackgroundColor(Color.BLACK.getRGB() & 0x40000000)
                                .setContent(
                                        new TextElement(titleFontRenderer, text)
                                                .setCompactHeight(true)));
            }

            if (entry.showOwner) {
                UUID owner = entry.entityLike.getOwner();
                if (owner != null) {
                    usernameCache.getUnchecked(owner).ifPresent(s -> {
                        flex.insertAt(0, new DivisionElement()
                                .setMargin(context.getScale())
                                .setBackgroundColor(Color.BLACK.getRGB() & 0x40000000)
                                .setContent(
                                        new TextElement(titleFontRenderer, StylizedText.of("Owner: " + s))
                                                .setCompactHeight(true)));
                    });
                }
            }

            if (entry.showEquippedItems && enchantmentFontRenderer != null) {
                entry.entityLike.collectEquipment(items);

                if (!items.isEmpty()) {
                    LivingEntity owner = entry.entityLike.getEquipmentOwner();

                    int maxEnchantments = 0;
                    enchantments.clear();
                    for (ItemStack item : items) {
                        List<EnchantmentEntry> entries = getEnchantments(item);
                        enchantments.add(entries);
                        if (entries.size() > maxEnchantments) {
                            maxEnchantments = entries.size();
                        }
                    }

                    TableElement table = new TableElement(1 + maxEnchantments, items.size());
                    //table.setBorderWidth(1);
                    for (int i = 0; i < items.size(); i++) {
                        List<EnchantmentEntry> itemEnchantments = enchantments.get(i);
                        for (int j = 0; j < itemEnchantments.size(); j++) {
                            table.setContent(maxEnchantments - itemEnchantments.size() + j, i,
                                    new TextElement(enchantmentFontRenderer, itemEnchantments.get(j).getText()));
                        }
                        table.setContent(maxEnchantments, i, new ItemStackElement(owner, items.get(i)));
                    }

                    flex.insertAt(0, table);
                }
            }

            context.render(flex, xc, yc - scale, HorizontalAlign.CENTER, VerticalAlign.BOTTOM);
        }
    }

    private void addDisconnectedPlayers(Vec3 view) {
        List<EntityLike> players = LogoutSpots.instance.getDisconnectedPlayers();
        if (players.isEmpty()) {
            return;
        }

        boolean drawTitles = false;
        boolean showDefaultNames = false;
        boolean showHp = false;
        boolean showEquippedItems = false;
        boolean useRaw = false;
        boolean showOwner = false;

        for (EntityEspConfig entityConfig : ConfigStore.instance.getConfig().entities.configs) {
            if (!entityConfig.enabled || !entityConfig.drawTitles) {
                continue;
            }
            if (entityConfig.clazz.isAssignableFrom(RemotePlayer.class)) {
                drawTitles = true;
                showDefaultNames |= entityConfig.showDefaultNames;
                useRaw |= entityConfig.useRawNames;
                showHp |= entityConfig.showHp;
                showEquippedItems |= entityConfig.showEquippedItems;
                showOwner |= entityConfig.showOwner;
            }
        }

        if (drawTitles) {
            for (EntityLike player : players) {
                Vec3 pos = player.getPosition().add(-view.x, -view.y + player.getHeight(), -view.z);
                entities.add(new EntityEntry(
                        player,
                        pos,
                        player.getPosition().distanceToSqr(view),
                        showDefaultNames,
                        useRaw,
                        showHp,
                        showEquippedItems,
                        showOwner,
                        null,
                        player.getTitleSuffix()));
            }
        }
    }

    private StylizedText getEntityText(EntityTitleConfig config, EntityEntry entry) {
        if (entry.title != null) {
            return entry.title;
        }

        Component component;
        if (entry.showDefaultNames) {
            component = entry.entityLike.getDisplayName();
        } else {
            component = entry.entityLike.hasCustomName() ? entry.entityLike.getDisplayName() : null;
        }

        StylizedText text = null;
        if (component != null) {
            if (entry.useRaw) {
                String value = component.getString();
                if (!value.isEmpty()) {
                    text = StylizedText.of(value);
                }
            } else {
                buffer.clear();
                builder.delete(0, builder.length());
                FormattedCharSequence sequence = component.getVisualOrderText();
                ColorHolder last = new ColorHolder();
                sequence.accept((unknown, style, character) -> {
                    int color = style.getColor() != null ? (style.getColor().getValue() | 0xFF000000) : Color.WHITE.getRGB();
                    if (last.color != color) {
                        if (!builder.isEmpty()) {
                            buffer.add(new StylizedTextChunk(builder.toString(), last.color));
                            builder.delete(0, builder.length());
                        }
                    }
                    last.color = color;
                    builder.append((char) character);
                    return true;
                });
                if (!builder.isEmpty()) {
                    buffer.add(new StylizedTextChunk(builder.toString(), last.color));
                }
                if (!buffer.isEmpty()) {
                    text = new StylizedText();
                    text.chunks.addAll(buffer);
                }
            }
        }

        if (entry.showHp && entry.entityLike.hasHealth()) {
            if (text == null) {
                text = new StylizedText();
                text.append(config.hpPrefix, 0xFFFF5555);
            } else {
                text.append(" " + config.hpPrefix, 0xFFFF5555);
            }
            text.append(String.valueOf(entry.entityLike.getHealth()), Color.WHITE.getRGB());

            int absorption = entry.entityLike.getAbsorption();
            if (absorption > 0) {
                text.append("+" + absorption, Color.YELLOW.getRGB());
            }
        }

        if (entry.suffix != null) {
            if (text == null) {
                text = entry.suffix;
            } else {
                text.append(entry.suffix);
            }
        }

        return text;
    }

    private List<EnchantmentEntry> getEnchantments(ItemStack itemStack) {
        if (!itemStack.isEnchanted()) {
            return List.of();
        }

        List<EnchantmentEntry> result = new ArrayList<>();
        ItemEnchantments enchantments = itemStack.getEnchantments();
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            Identifier id = holder.unwrapKey().get().identifier();
            int level = enchantments.getLevel(holder);
            result.add(new EnchantmentEntry(id, level));
        }

        result.sort(Comparator.comparingInt(e -> e.priority));
        return result;
    }

    private record EntityEntry(
            EntityLike entityLike,
            Vec3 position,
            double distanceSqr,
            boolean showDefaultNames,
            boolean useRaw,
            boolean showHp,
            boolean showEquippedItems,
            boolean showOwner,
            StylizedText title,
            StylizedText suffix) {}

    private static class EnchantmentEntry {

        private static final Map<Identifier, EnchantmentDisplayEntry> displayMap = Map.ofEntries(
                Map.entry(Enchantments.PROTECTION.identifier(), new EnchantmentDisplayEntry("Pr")),
                Map.entry(Enchantments.FIRE_PROTECTION.identifier(), new EnchantmentDisplayEntry("FP")),
                Map.entry(Enchantments.BLAST_PROTECTION.identifier(), new EnchantmentDisplayEntry("BP")),
                Map.entry(Enchantments.PROJECTILE_PROTECTION.identifier(), new EnchantmentDisplayEntry("PP")),

                Map.entry(Enchantments.THORNS.identifier(), new EnchantmentDisplayEntry("Th")),

                Map.entry(Enchantments.FEATHER_FALLING.identifier(), new EnchantmentDisplayEntry("Fe")),
                Map.entry(Enchantments.RESPIRATION.identifier(), new EnchantmentDisplayEntry("Re")),
                Map.entry(Enchantments.AQUA_AFFINITY.identifier(), new EnchantmentDisplayEntry("Aq")),
                Map.entry(Enchantments.DEPTH_STRIDER.identifier(), new EnchantmentDisplayEntry("De")),
                Map.entry(Enchantments.FROST_WALKER.identifier(), new EnchantmentDisplayEntry("Fr")),
                Map.entry(Enchantments.SOUL_SPEED.identifier(), new EnchantmentDisplayEntry("So")),
                Map.entry(Enchantments.SWIFT_SNEAK.identifier(), new EnchantmentDisplayEntry("Sn")),

                Map.entry(Enchantments.SHARPNESS.identifier(), new EnchantmentDisplayEntry("Sh")),
                Map.entry(Enchantments.SMITE.identifier(), new EnchantmentDisplayEntry("Sm")),
                Map.entry(Enchantments.BANE_OF_ARTHROPODS.identifier(), new EnchantmentDisplayEntry("Ar")),
                Map.entry(Enchantments.FIRE_ASPECT.identifier(), new EnchantmentDisplayEntry("Fi")),
                Map.entry(Enchantments.KNOCKBACK.identifier(), new EnchantmentDisplayEntry("Kn")),
                Map.entry(Enchantments.LOOTING.identifier(), new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.SWEEPING_EDGE.identifier(), new EnchantmentDisplayEntry("Sw")),
                Map.entry(Enchantments.DENSITY.identifier(), new EnchantmentDisplayEntry("Dn")),
                Map.entry(Enchantments.BREACH.identifier(), new EnchantmentDisplayEntry("Br")),
                Map.entry(Enchantments.WIND_BURST.identifier(), new EnchantmentDisplayEntry("Wi")),
                Map.entry(Enchantments.LUNGE.identifier(), new EnchantmentDisplayEntry("Lu")),

                Map.entry(Enchantments.SILK_TOUCH.identifier(), new EnchantmentDisplayEntry("Si")),
                Map.entry(Enchantments.FORTUNE.identifier(), new EnchantmentDisplayEntry("Fo")),
                Map.entry(Enchantments.EFFICIENCY.identifier(), new EnchantmentDisplayEntry("Ef")),

                Map.entry(Enchantments.POWER.identifier(), new EnchantmentDisplayEntry("Po")),
                Map.entry(Enchantments.PUNCH.identifier(), new EnchantmentDisplayEntry("Pu")),
                Map.entry(Enchantments.INFINITY.identifier(), new EnchantmentDisplayEntry("In")),
                Map.entry(Enchantments.FLAME.identifier(), new EnchantmentDisplayEntry("Fl")),
                Map.entry(Enchantments.LUCK_OF_THE_SEA.identifier(), new EnchantmentDisplayEntry("Lc")),
                Map.entry(Enchantments.LURE.identifier(), new EnchantmentDisplayEntry("Lr")),
                Map.entry(Enchantments.LOYALTY.identifier(), new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.IMPALING.identifier(), new EnchantmentDisplayEntry("Im")),
                Map.entry(Enchantments.RIPTIDE.identifier(), new EnchantmentDisplayEntry("Ri")),
                Map.entry(Enchantments.CHANNELING.identifier(), new EnchantmentDisplayEntry("Ch")),
                Map.entry(Enchantments.MULTISHOT.identifier(), new EnchantmentDisplayEntry("Mu")),
                Map.entry(Enchantments.QUICK_CHARGE.identifier(), new EnchantmentDisplayEntry("Qu")),
                Map.entry(Enchantments.PIERCING.identifier(), new EnchantmentDisplayEntry("Pi")),

                Map.entry(Enchantments.UNBREAKING.identifier(), new EnchantmentDisplayEntry("Un")),

                Map.entry(Enchantments.MENDING.identifier(), new EnchantmentDisplayEntry("Me")),
                Map.entry(Enchantments.VANISHING_CURSE.identifier(), new EnchantmentDisplayEntry("Va", Color.RED)),
                Map.entry(Enchantments.BINDING_CURSE.identifier(), new EnchantmentDisplayEntry("Bi", Color.RED)));

        public final String text;
        public final int level;
        public final Color color;
        public final int priority;

        public EnchantmentEntry(Identifier id, int level) {
            EnchantmentDisplayEntry entry = displayMap.get(id);
            if (entry != null) {
                text = entry.text;
                color = entry.color;
                priority = entry.priority;
            } else {
                text = id.toString();
                color = Color.YELLOW;
                priority = 100;
            }
            this.level = level;
        }

        public StylizedText getText() {
            StylizedText stylizedText = StylizedText.of(text, color.getRGB());
            stylizedText.append(Integer.toString(level), 0xFF00FFFF);
            return stylizedText;
        }
    }

    private record EnchantmentDisplayEntry(String text, Color color, int priority) {

        private static int index;

        public EnchantmentDisplayEntry(String text, Color color) {
            this(text, color, ++index);
        }

        public EnchantmentDisplayEntry(String text) {
            this(text, Color.WHITE, ++index);
        }
    }

    private static class ColorHolder {
        public int color;
    }
}