package com.zergatul.cheatutils.controllers;

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
import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.font.StylizedTextChunk;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.mixins.common.accessors.ProjectileAccessor;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
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

public class EntityTitleController {

    public static final EntityTitleController instance = new EntityTitleController();

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
                        ProfileResult result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(uuid, false);
                        if (result == null) {
                            usernameCache.put(uuid, Optional.of(uuid.toString()));
                        } else {
                            usernameCache.put(uuid, Optional.of(result.profile().getName()));
                        }
                    });
                    return Optional.of("loading...");
                }
            });

    private final List<EntityEntry> entities = new ArrayList<>();
    private GlyphFontRenderer fontRenderer;
    private GlyphFontRenderer enchFontRenderer;

    private EntityTitleController() {
        Events.AfterRenderWorld.add(this::onRenderWorld);
        Events.PreRenderGui.add(this::onRenderGui);
    }

    public void onFontChange(EntityTitleConfig config) {
        TickEndExecutor.instance.execute(() -> {
            if (fontRenderer != null) {
                fontRenderer.dispose();
            }
            fontRenderer = new GlyphFontRenderer(new Font("Consolas", Font.PLAIN, config.fontSize), config.antiAliasing);
        });
    }

    public void onEnchantmentFontChange(EntityTitleConfig config) {
        TickEndExecutor.instance.execute(() -> {
            if (enchFontRenderer != null) {
                enchFontRenderer.dispose();
            }
            enchFontRenderer = new GlyphFontRenderer(new Font("Consolas", Font.PLAIN, config.enchFontSize), config.enchAntiAliasing);
        });
    }

    private void onRenderWorld(RenderWorldLastEvent event) {
        entities.clear();
        if (mc.level == null) {
            return;
        }

        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        ImmutableList<EntityEspConfig> entityConfigs = ConfigStore.instance.getConfig().entities.configs;

        Vec3 view = event.getCamera().getPosition();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                continue;
            }

            Vec3 pos = entity.getPosition(event.getTickDelta());
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
                        entity,
                        pos,
                        distanceSqr,
                        showDefaultNames,
                        useRaw,
                        showHp,
                        showEquippedItems,
                        showOwner,
                        title));
            }
        }

        entities.sort((e1, e2) -> -Double.compare(e1.distanceSqr, e2.distanceSqr));
    }

    public void onRenderGui(RenderGuiEvent event) {
        if (fontRenderer == null) {
            return;
        }

        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);

        int scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

        List<ItemStack> items = new ArrayList<>();
        List<List<EnchantmentEntry>> enchantments = new ArrayList<>();
        List<TextBounds[]> enchantmentBounds = new ArrayList<>();
        List<Integer> enchantmentWidths = new ArrayList<>();
        List<Integer> enchantmentTextWidths = new ArrayList<>();

        for (EntityEntry entry : entities) {
            Vector4f v1 = event.getWorldPoseMatrix().transform(new Vector4f((float)entry.position.x, (float)entry.position.y, (float)entry.position.z, 1));
            Vector4f v2 = event.getWorldProjectionMatrix().transform(v1);
            if (v2.z <= 0) {
                continue; // behind
            }

            int xc = Math.round(v2.x / v2.w * halfScrWidth);
            int yc = Math.round(-v2.y / v2.w * halfScrHeight);

            StylizedText text = getEntityText(entry);
            if (text != null) {
                TextBounds bounds = fontRenderer.getTextSize(text);
                if (bounds.width() > 0) {
                    int width = bounds.width();
                    int height = bounds.height();

                    int xp = xc - width / 2;
                    yc -= height;
                    int yp = yc;

                    int rx1 = xp - scale;
                    int rx2 = xp + width + scale;
                    int ry1 = yp + (bounds.top() - scale);
                    int ry2 = yp + height - (bounds.bottom() - scale);
                    int width2 = rx2 - rx1;
                    int height2 = ry2 - ry1;

                    MainFrameBuffer.enter();
                    Primitives.fill(matrix, rx1, ry1, width2, height2, Color.BLACK.getRGB() & 0x40000000);

                    for (StylizedTextChunk chunk : text.chunks) {
                        int color = chunk.getColor();
                        fontRenderer.drawText(matrix, chunk.text(), xp, yp, color);
                        xp += fontRenderer.getTextSize(chunk.text()).width();
                    }

                    MainFrameBuffer.exit();
                }
            }

            if (entry.showOwner) {
                UUID owner = getOwner(entry.entity);
                if (owner != null) {
                    Optional<String> nameOpt = usernameCache.getUnchecked(owner);
                    if (nameOpt.isPresent()) {
                        String ownerText = "Owner: " + nameOpt.get();
                        TextBounds bounds = fontRenderer.getTextSize(ownerText);
                        int width = bounds.width();
                        int height = bounds.height();

                        int xp = xc - width / 2;
                        yc -= height;
                        int yp = yc;

                        int rx1 = xp - scale;
                        int rx2 = xp + width + scale;
                        int ry1 = yp + (bounds.top() - scale);
                        int ry2 = yp + height - (bounds.bottom() - scale);
                        int width2 = rx2 - rx1;
                        int height2 = ry2 - ry1;

                        MainFrameBuffer.enter();
                        Primitives.fill(matrix, rx1, ry1, width2, height2, Color.BLACK.getRGB() & 0x40000000);
                        fontRenderer.drawText(matrix, ownerText, xp, yp, 0xFFFFFFFF);
                        MainFrameBuffer.exit();
                    }
                }
            }

            if (entry.showEquippedItems && enchFontRenderer != null && entry.entity instanceof LivingEntity livingEntity) {
                ItemStack mainHand = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
                ItemStack head = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack legs = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack feet = livingEntity.getItemBySlot(EquipmentSlot.FEET);
                ItemStack offhand = livingEntity.getItemBySlot(EquipmentSlot.OFFHAND);

                items.clear();
                if (!mainHand.isEmpty()) {
                    items.add(mainHand);
                }
                if (!head.isEmpty()) {
                    items.add(head);
                }
                if (!chest.isEmpty()) {
                    items.add(chest);
                }
                if (!legs.isEmpty()) {
                    items.add(legs);
                }
                if (!feet.isEmpty()) {
                    items.add(feet);
                }
                if (!offhand.isEmpty()) {
                    items.add(offhand);
                }

                if (!items.isEmpty()) {
                    enchantments.clear();
                    enchantmentWidths.clear();
                    enchantmentTextWidths.clear();
                    enchantmentBounds.clear();
                    for (ItemStack item : items) {
                        List<EnchantmentEntry> entries = getEnchantments(item);
                        enchantments.add(entries);

                        TextBounds[] bounds = new TextBounds[entries.size()];
                        int maxWidth = 16 * scale;
                        int maxTextWidth = 0;
                        for (int i = 0; i < bounds.length; i++) {
                            EnchantmentEntry ee = entries.get(i);
                            bounds[i] = enchFontRenderer.getTextSize(ee.text + ee.level);
                            int width = bounds[i].width();
                            if (width > maxWidth) {
                                maxWidth = width;
                            }
                            if (width > maxTextWidth) {
                                maxTextWidth = width;
                            }
                        }

                        enchantmentWidths.add(maxWidth);
                        enchantmentTextWidths.add(maxTextWidth);
                        enchantmentBounds.add(bounds);
                    }

                    int width = 0;
                    for (int ew : enchantmentWidths) {
                        width += ew;
                    }
                    int height = 16 * scale;

                    int xp = xc - width / 2 + halfScrWidth;
                    yc -= height;
                    int yp = yc + halfScrHeight;

                    int xpl = xp;

                    GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
                    for (int i = 0; i < items.size(); i++) {
                        int xCenterOffset = enchantmentTextWidths.get(i) > 16 * scale ? (enchantmentTextWidths.get(i) - 16 * scale) / 2 : 0;
                        event.graphics().pose().pushPose();
                        event.graphics().pose().setIdentity();
                        event.graphics().pose().translate(1d * (xpl + xCenterOffset) / scale, 1d * yp / scale, 0);
                        event.getGuiGraphics().renderItem(livingEntity, items.get(i), 0, 0, 0);
                        event.getGuiGraphics().renderItemDecorations(mc.font, items.get(i), 0, 0);
                        event.graphics().pose().popPose();
                        xpl += enchantmentWidths.get(i);
                    }
                    GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);

                    xpl = xp - halfScrWidth;
                    MainFrameBuffer.enter();
                    for (int i = 0; i < items.size(); i++) {
                        List<EnchantmentEntry> entries = enchantments.get(i);
                        TextBounds[] bounds = enchantmentBounds.get(i);
                        int ypl = yp - halfScrHeight;
                        int xCenterOffset = enchantmentTextWidths.get(i) < 16 * scale ? (16 * scale - enchantmentTextWidths.get(i)) / 2 : 0;
                        for (int j = entries.size() - 1; j >= 0; j--) {
                            EnchantmentEntry e = entries.get(j);
                            ypl -= bounds[j].height();

                            TextBounds bound = enchFontRenderer.getTextSize(e.text);
                            enchFontRenderer.drawText(matrix, e.text, (float)(xpl + xCenterOffset), (float)ypl, e.color.getRGB());
                            enchFontRenderer.drawText(matrix, Integer.toString(e.level), (float) (xpl + bound.width() + xCenterOffset), (float)ypl, 0xFF00FFFF);
                        }
                        xpl += enchantmentWidths.get(i);
                    }
                    MainFrameBuffer.exit();
                }
            }
        }

        GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
    }

    private StylizedText getEntityText(EntityEntry entry) {
        if (entry.title != null) {
            return entry.title;
        }

        Component component;
        if (entry.showDefaultNames) {
            component = entry.entity.getDisplayName();
        } else {
            component = entry.entity.hasCustomName() || entry.entity instanceof Player ? entry.entity.getDisplayName() : null;
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
                StyleHolder last = new StyleHolder();
                sequence.accept((unknown, style, character) -> {
                    if (last.value != style) {
                        if (!builder.isEmpty()) {
                            buffer.add(new StylizedTextChunk(builder.toString(), last.value));
                            builder.delete(0, builder.length());
                        }
                    }
                    last.value = style;
                    builder.append((char) character);
                    return true;
                });
                if (!builder.isEmpty()) {
                    buffer.add(new StylizedTextChunk(builder.toString(), last.value));
                }
                if (!buffer.isEmpty()) {
                    text = new StylizedText();
                    text.chunks.addAll(buffer);
                }
            }
        }

        if (entry.showHp && entry.entity instanceof LivingEntity living) {
            if (text == null) {
                text = new StylizedText();
                text.append("♥", Style.EMPTY.withColor(ChatFormatting.RED));
            } else {
                text.append(" ♥", Style.EMPTY.withColor(ChatFormatting.RED));
            }
            text.append(String.valueOf((int)living.getHealth()), Style.EMPTY);
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
            ResourceLocation id = holder.unwrapKey().get().location();
            int level = enchantments.getLevel(holder);
            result.add(new EnchantmentEntry(id, level));
        }

        result.sort(Comparator.comparingInt(e -> e.priority));
        return result;
    }

    private UUID getOwner(Entity entity) {
        if (entity instanceof TamableAnimal animal) {
            EntityReference<LivingEntity> reference = animal.getOwnerReference();
            return reference != null ? reference.getUUID() : null;
        }
        if (entity instanceof AbstractHorse horse) {
            EntityReference<LivingEntity> reference = horse.getOwnerReference();
            return reference != null ? reference.getUUID() : null;
        }
        if (entity instanceof Projectile projectile) {
            ProjectileAccessor projectileMixin = (ProjectileAccessor) projectile;
            return projectileMixin.getOwnerUUID_CU();
        }
        // fox?
        return null;
    }

    private record EntityEntry(
            Entity entity,
            Vec3 position,
            double distanceSqr,
            boolean showDefaultNames,
            boolean useRaw,
            boolean showHp,
            boolean showEquippedItems,
            boolean showOwner,
            StylizedText title) {}

    private static class EnchantmentEntry {

        private static final Map<ResourceLocation, EnchantmentDisplayEntry> displayMap = Map.ofEntries(
                Map.entry(Enchantments.PROTECTION.location(), new EnchantmentDisplayEntry("Pr")),
                Map.entry(Enchantments.FIRE_PROTECTION.location(), new EnchantmentDisplayEntry("FP")),
                Map.entry(Enchantments.BLAST_PROTECTION.location(), new EnchantmentDisplayEntry("BP")),
                Map.entry(Enchantments.PROJECTILE_PROTECTION.location(), new EnchantmentDisplayEntry("PP")),

                Map.entry(Enchantments.THORNS.location(), new EnchantmentDisplayEntry("Th")),

                Map.entry(Enchantments.FEATHER_FALLING.location(), new EnchantmentDisplayEntry("Fe")),
                Map.entry(Enchantments.RESPIRATION.location(), new EnchantmentDisplayEntry("Re")),
                Map.entry(Enchantments.AQUA_AFFINITY.location(), new EnchantmentDisplayEntry("Aq")),
                Map.entry(Enchantments.DEPTH_STRIDER.location(), new EnchantmentDisplayEntry("De")),
                Map.entry(Enchantments.FROST_WALKER.location(), new EnchantmentDisplayEntry("Fr")),
                Map.entry(Enchantments.SOUL_SPEED.location(), new EnchantmentDisplayEntry("So")),
                Map.entry(Enchantments.SWIFT_SNEAK.location(), new EnchantmentDisplayEntry("Sn")),

                Map.entry(Enchantments.SHARPNESS.location(), new EnchantmentDisplayEntry("Sh")),
                Map.entry(Enchantments.SMITE.location(), new EnchantmentDisplayEntry("Sm")),
                Map.entry(Enchantments.BANE_OF_ARTHROPODS.location(), new EnchantmentDisplayEntry("Ar")),
                Map.entry(Enchantments.FIRE_ASPECT.location(), new EnchantmentDisplayEntry("Fi")),
                Map.entry(Enchantments.KNOCKBACK.location(), new EnchantmentDisplayEntry("Kn")),
                Map.entry(Enchantments.LOOTING.location(), new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.SWEEPING_EDGE.location(), new EnchantmentDisplayEntry("Sw")),
                Map.entry(Enchantments.DENSITY.location(), new EnchantmentDisplayEntry("Dn")),
                Map.entry(Enchantments.BREACH.location(), new EnchantmentDisplayEntry("Br")),
                Map.entry(Enchantments.WIND_BURST.location(), new EnchantmentDisplayEntry("Wi")),

                Map.entry(Enchantments.SILK_TOUCH.location(), new EnchantmentDisplayEntry("Si")),
                Map.entry(Enchantments.FORTUNE.location(), new EnchantmentDisplayEntry("Fo")),
                Map.entry(Enchantments.EFFICIENCY.location(), new EnchantmentDisplayEntry("Ef")),

                Map.entry(Enchantments.POWER.location(), new EnchantmentDisplayEntry("Po")),
                Map.entry(Enchantments.PUNCH.location(), new EnchantmentDisplayEntry("Pu")),
                Map.entry(Enchantments.INFINITY.location(), new EnchantmentDisplayEntry("In")),
                Map.entry(Enchantments.FLAME.location(), new EnchantmentDisplayEntry("Fl")),
                Map.entry(Enchantments.LUCK_OF_THE_SEA.location(), new EnchantmentDisplayEntry("Lc")),
                Map.entry(Enchantments.LURE.location(), new EnchantmentDisplayEntry("Lr")),
                Map.entry(Enchantments.LOYALTY.location(), new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.IMPALING.location(), new EnchantmentDisplayEntry("Im")),
                Map.entry(Enchantments.RIPTIDE.location(), new EnchantmentDisplayEntry("Ri")),
                Map.entry(Enchantments.CHANNELING.location(), new EnchantmentDisplayEntry("Ch")),
                Map.entry(Enchantments.MULTISHOT.location(), new EnchantmentDisplayEntry("Mu")),
                Map.entry(Enchantments.QUICK_CHARGE.location(), new EnchantmentDisplayEntry("Qu")),
                Map.entry(Enchantments.PIERCING.location(), new EnchantmentDisplayEntry("Pi")),

                Map.entry(Enchantments.UNBREAKING.location(), new EnchantmentDisplayEntry("Un")),

                Map.entry(Enchantments.MENDING.location(), new EnchantmentDisplayEntry("Me")),
                Map.entry(Enchantments.VANISHING_CURSE.location(), new EnchantmentDisplayEntry("Va", Color.RED)),
                Map.entry(Enchantments.BINDING_CURSE.location(), new EnchantmentDisplayEntry("Bi", Color.RED)));

        public final String text;
        public final int level;
        public final Color color;
        public final int priority;

        public EnchantmentEntry(ResourceLocation id, int level) {
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

    private static class StyleHolder {
        public Style value;
    }
}