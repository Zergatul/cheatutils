package com.zergatul.cheatutils.controllers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTitleConfig;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.mixins.common.accessors.ProjectileAccessor;
import com.zergatul.cheatutils.render.GlStates;
import com.zergatul.cheatutils.render.ItemRenderHelper;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EntityTitleController {

    public static final EntityTitleController instance = new EntityTitleController();

    private final Minecraft mc = Minecraft.getInstance();

    private final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public Optional<String> load(UUID uuid) {
                    CompletableFuture.runAsync(() -> {
                        GameProfile playerProfile = new GameProfile(uuid, null);
                        playerProfile = Minecraft.getInstance().getMinecraftSessionService().fillProfileProperties(playerProfile, false);
                        if (playerProfile.getName() == null) {
                            usernameCache.put(uuid, Optional.of(uuid.toString()));
                        } else {
                            usernameCache.put(uuid, Optional.of(playerProfile.getName()));
                        }
                    });
                    return Optional.of("loading...");
                }
            });

    private final List<EntityEntry> entities = new ArrayList<>();
    private GlyphFontRenderer fontRenderer;
    private GlyphFontRenderer enchFontRenderer;

    private EntityTitleController() {
        Events.RenderWorldLast.add(this::onRenderWorld);
        Events.PreRenderGui.add(this::onRenderGui);
    }

    public void onFontChange(EntityTitleConfig config) {
        RenderSystem.recordRenderCall(() -> {
            if (fontRenderer != null) {
                fontRenderer.dispose();
            }
            fontRenderer = new GlyphFontRenderer(new Font("Consolas", Font.PLAIN, config.fontSize), config.antiAliasing);
        });
    }

    public void onEnchantmentFontChange(EntityTitleConfig config) {
        RenderSystem.recordRenderCall(() -> {
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

        ImmutableList<EntityTracerConfig> entityConfigs = ConfigStore.instance.getConfig().entities.configs;

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
            boolean showOwner = false;
            for (EntityTracerConfig entityConfig : entityConfigs) {
                if (!entityConfig.enabled || !entityConfig.drawTitles) {
                    continue;
                }
                if (!entityConfig.isValidEntity(entity)) {
                    continue;
                }

                if (distanceSqr < entityConfig.maxDistance * entityConfig.maxDistance) {
                    drawTitles = true;
                    showDefaultNames |= entityConfig.showDefaultNames;
                    showHp |= entityConfig.showHp;
                    showEquippedItems |= entityConfig.showEquippedItems;
                    showOwner |= entityConfig.showOwner;
                }
            }

            if (drawTitles) {
                pos = pos.add(-view.x, -view.y + entity.getBbHeight(), -view.z);
                entities.add(new EntityEntry(
                        entity,
                        pos,
                        distanceSqr,
                        showDefaultNames,
                        showHp,
                        showEquippedItems,
                        showOwner));
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

        double scale = mc.getWindow().getGuiScale();
        double invScale = 1 / scale;
        double scaledHalfWidth = mc.getWindow().getWidth() * invScale / 2;
        double scaledHalfHeight = mc.getWindow().getHeight() * invScale / 2;
        List<ItemStack> items = new ArrayList<>();
        List<List<EnchantmentEntry>> enchantments = new ArrayList<>();
        List<TextBounds[]> enchantmentBounds = new ArrayList<>();
        List<Integer> enchantmentWidths = new ArrayList<>();
        List<Integer> enchantmentTextWidths = new ArrayList<>();

        event.getMatrixStack().pushPose();
        event.getMatrixStack().last().pose().translate(new Vector3f((float)scaledHalfWidth, (float)scaledHalfHeight, 0));

        for (EntityEntry entry : entities) {
            Vector4f v1 = new Vector4f((float)entry.position.x, (float)entry.position.y, (float)entry.position.z, 1);
            v1.transform(event.getWorldPoseMatrix());

            Vector4f v2 = new Vector4f(v1.x(), v1.y(), v1.z(), v1.z());
            v2.transform(event.getWorldProjectionMatrix());
            if (v2.z() <= 0) {
                continue; // behind
            }

            double xc = v2.x() / v2.w() * scaledHalfWidth;
            double yc = -v2.y() / v2.w() * scaledHalfHeight;

            String text = getEntityText(entry);
            if (text != null) {
                TextBounds bounds = fontRenderer.getTextSize(text);
                double width = bounds.width() * invScale;
                double height = bounds.height() * invScale;

                double xp = xc - width / 2;
                yc -= height;
                double yp = yc;

                double horizontalPadding = scale;
                double verticalPadding = scale;
                double rx1 = xp - horizontalPadding * invScale;
                double rx2 = xp + width + horizontalPadding * invScale;
                double ry1 = yp + (bounds.top() - verticalPadding) * invScale;
                double ry2 = yp + height - (bounds.bottom() - verticalPadding) * invScale;
                Primitives.fill(event.getMatrixStack(), rx1, ry1, rx2, ry2, Color.BLACK.getRGB() & 0x40000000);

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                fontRenderer.drawText(event.getMatrixStack(), text, (float)xp, (float)yp, invScale);
            }

            if (entry.showOwner) {
                UUID owner = getOwner(entry.entity);
                if (owner != null) {
                    Optional<String> nameOpt = usernameCache.getUnchecked(owner);
                    if (nameOpt.isPresent()) {
                        String ownerText = "Owner: " + nameOpt.get();
                        TextBounds bounds = fontRenderer.getTextSize(ownerText);
                        double width = bounds.width() * invScale;
                        double height = bounds.height() * invScale;

                        double xp = xc - width / 2;
                        yc -= height;
                        double yp = yc;

                        double horizontalPadding = scale;
                        double verticalPadding = scale;
                        double rx1 = xp - horizontalPadding * invScale;
                        double rx2 = xp + width + horizontalPadding * invScale;
                        double ry1 = yp + (bounds.top() - verticalPadding) * invScale;
                        double ry2 = yp + height - (bounds.bottom() - verticalPadding) * invScale;
                        Primitives.fill(event.getMatrixStack(), rx1, ry1, rx2, ry2, Color.BLACK.getRGB() & 0x40000000);

                        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                        fontRenderer.drawText(event.getMatrixStack(), ownerText, (float)xp, (float)yp, invScale);
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

                if (items.size() > 0) {
                    enchantments.clear();
                    enchantmentWidths.clear();
                    enchantmentTextWidths.clear();
                    enchantmentBounds.clear();
                    for (ItemStack item : items) {
                        List<EnchantmentEntry> entries = getEnchantments(item);
                        enchantments.add(entries);

                        TextBounds[] bounds = new TextBounds[entries.size()];
                        int maxWidth = 16;
                        int maxTextWidth = 0;
                        for (int i = 0; i < bounds.length; i++) {
                            EnchantmentEntry ee = entries.get(i);
                            bounds[i] = enchFontRenderer.getTextSize(ee.text + ee.level);
                            int width = Mth.ceil(bounds[i].width() * invScale);
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

                    double width = 0;
                    for (int ew : enchantmentWidths) {
                        width += ew;
                    }
                    double height = 16;

                    double xp = xc - width / 2 + scaledHalfWidth;
                    yc -= height;
                    double yp = yc + scaledHalfHeight;

                    GlStates.setupOverlayRenderState(true, false);

                    mc.getItemRenderer().blitOffset = 0;
                    double xpl = xp;
                    for (int i = 0; i < items.size(); i++) {
                        double xCenterOffset = enchantmentTextWidths.get(i) > 16 ? (enchantmentTextWidths.get(i) - 16) / 2d : 0;
                        ItemRenderHelper.renderItem(livingEntity, items.get(i), xpl + xCenterOffset, yp, 0);
                        xpl += enchantmentWidths.get(i);
                    }

                    xpl = xp - scaledHalfWidth;
                    for (int i = 0; i < items.size(); i++) {
                        List<EnchantmentEntry> entries = enchantments.get(i);
                        TextBounds[] bounds = enchantmentBounds.get(i);
                        double ypl = yp - scaledHalfHeight;
                        double xCenterOffset = enchantmentTextWidths.get(i) < 16 ? (16 - enchantmentTextWidths.get(i)) / 2d : 0;
                        for (int j = entries.size() - 1; j >= 0; j--) {
                            EnchantmentEntry e = entries.get(j);
                            ypl -= bounds[j].height() * invScale;

                            TextBounds bound = enchFontRenderer.getTextSize(e.text);
                            RenderSystem.setShaderColor(
                                    e.color.getRed() / 255f,
                                    e.color.getGreen() / 255f,
                                    e.color.getBlue() / 255f,
                                    e.color.getAlpha() / 255f);
                            enchFontRenderer.drawText(event.getMatrixStack(), e.text, (float)(xpl + xCenterOffset), (float)ypl, invScale);

                            RenderSystem.setShaderColor(0f, 1f, 1f, 1f);
                            enchFontRenderer.drawText(event.getMatrixStack(), Integer.toString(e.level), (float) (xpl + bound.width() * invScale + xCenterOffset), (float)ypl, invScale);
                        }
                        xpl += enchantmentWidths.get(i);
                    }
                }
            }
        }

        event.getMatrixStack().popPose();
    }

    private String getEntityText(EntityEntry entry) {
        String result;
        if (entry.showDefaultNames) {
            result = entry.entity.getDisplayName().getString();
        } else {
            result = entry.entity.hasCustomName() || entry.entity instanceof Player ? entry.entity.getDisplayName().getString() : "";
        }

        if (entry.showHp && entry.entity instanceof LivingEntity living) {
            result += "♥" + (int)living.getHealth();
        }
        return result.length() == 0 ? null : result;
        /*String tags = String.join(";", entity.getTags());
        if (entity instanceof LivingEntity living) {
            for (AttributeInstance attr: living.getAttributes().getSyncableAttributes()) {
                tags += attr.getAttribute().getDescriptionId() + "=" + attr.getValue() + ";";
            }
            tags += "!";
            for (AttributeInstance attr: living.getAttributes().getDirtyAttributes()) {
                tags += attr.getAttribute().getDescriptionId() + "=" + attr.getValue() + ";";
            }
        }
        return tags;*/
    }

    private List<EnchantmentEntry> getEnchantments(ItemStack itemStack) {
        if (!itemStack.isEnchanted()) {
            return List.of();
        }

        List<EnchantmentEntry> enchantments = new ArrayList<>();
        ListTag list = itemStack.getEnchantmentTags();
        for(int i = 0; i < list.size(); ++i) {
            CompoundTag compound = list.getCompound(i);
            ResourceLocation id = EnchantmentHelper.getEnchantmentId(compound);
            int level = EnchantmentHelper.getEnchantmentLevel(compound);
            enchantments.add(new EnchantmentEntry(id, level));
        }

        enchantments.sort(Comparator.comparingInt(e -> e.priority));
        return enchantments;
    }

    private UUID getOwner(Entity entity) {
        if (entity instanceof TamableAnimal animal) {
            return animal.getOwnerUUID();
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.getOwnerUUID();
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
            boolean showHp,
            boolean showEquippedItems,
            boolean showOwner) {}

    private static class EnchantmentEntry {

        private static final Map<Enchantment, EnchantmentDisplayEntry> displayMap = Map.ofEntries(
                Map.entry(Enchantments.ALL_DAMAGE_PROTECTION, new EnchantmentDisplayEntry("Pr")),
                Map.entry(Enchantments.FIRE_PROTECTION, new EnchantmentDisplayEntry("FP")),
                Map.entry(Enchantments.BLAST_PROTECTION, new EnchantmentDisplayEntry("BP")),
                Map.entry(Enchantments.PROJECTILE_PROTECTION, new EnchantmentDisplayEntry("PP")),

                Map.entry(Enchantments.THORNS, new EnchantmentDisplayEntry("Th")),

                Map.entry(Enchantments.FALL_PROTECTION, new EnchantmentDisplayEntry("Fe")),
                Map.entry(Enchantments.RESPIRATION, new EnchantmentDisplayEntry("Re")),
                Map.entry(Enchantments.AQUA_AFFINITY, new EnchantmentDisplayEntry("Aq")),
                Map.entry(Enchantments.DEPTH_STRIDER, new EnchantmentDisplayEntry("De")),
                Map.entry(Enchantments.FROST_WALKER, new EnchantmentDisplayEntry("Fr")),
                Map.entry(Enchantments.SOUL_SPEED, new EnchantmentDisplayEntry("So")),
                Map.entry(Enchantments.SWIFT_SNEAK, new EnchantmentDisplayEntry("Sn")),

                Map.entry(Enchantments.SHARPNESS, new EnchantmentDisplayEntry("Sh")),
                Map.entry(Enchantments.SMITE, new EnchantmentDisplayEntry("Sm")),
                Map.entry(Enchantments.BANE_OF_ARTHROPODS, new EnchantmentDisplayEntry("Ar")),
                Map.entry(Enchantments.FIRE_ASPECT, new EnchantmentDisplayEntry("Fi")),
                Map.entry(Enchantments.KNOCKBACK, new EnchantmentDisplayEntry("Kn")),
                Map.entry(Enchantments.MOB_LOOTING, new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.SWEEPING_EDGE, new EnchantmentDisplayEntry("Sw")),

                Map.entry(Enchantments.SILK_TOUCH, new EnchantmentDisplayEntry("Si")),
                Map.entry(Enchantments.BLOCK_FORTUNE, new EnchantmentDisplayEntry("Fo")),
                Map.entry(Enchantments.BLOCK_EFFICIENCY, new EnchantmentDisplayEntry("Ef")),

                Map.entry(Enchantments.POWER_ARROWS, new EnchantmentDisplayEntry("Po")),
                Map.entry(Enchantments.PUNCH_ARROWS, new EnchantmentDisplayEntry("Pu")),
                Map.entry(Enchantments.INFINITY_ARROWS, new EnchantmentDisplayEntry("In")),
                Map.entry(Enchantments.FLAMING_ARROWS, new EnchantmentDisplayEntry("Fl")),
                Map.entry(Enchantments.FISHING_LUCK, new EnchantmentDisplayEntry("Lc")),
                Map.entry(Enchantments.FISHING_SPEED, new EnchantmentDisplayEntry("Lr")),
                Map.entry(Enchantments.LOYALTY, new EnchantmentDisplayEntry("Lo")),
                Map.entry(Enchantments.IMPALING, new EnchantmentDisplayEntry("Im")),
                Map.entry(Enchantments.RIPTIDE, new EnchantmentDisplayEntry("Ri")),
                Map.entry(Enchantments.CHANNELING, new EnchantmentDisplayEntry("Ch")),
                Map.entry(Enchantments.MULTISHOT, new EnchantmentDisplayEntry("Mu")),
                Map.entry(Enchantments.QUICK_CHARGE, new EnchantmentDisplayEntry("Qu")),
                Map.entry(Enchantments.PIERCING, new EnchantmentDisplayEntry("Pi")),

                Map.entry(Enchantments.UNBREAKING, new EnchantmentDisplayEntry("Un")),

                Map.entry(Enchantments.MENDING, new EnchantmentDisplayEntry("Me")),
                Map.entry(Enchantments.VANISHING_CURSE, new EnchantmentDisplayEntry("Va", Color.RED)),
                Map.entry(Enchantments.BINDING_CURSE, new EnchantmentDisplayEntry("Bi", Color.RED)));

        public final String text;
        public final int level;
        public final Color color;
        public final int priority;

        public EnchantmentEntry(ResourceLocation id, int level) {
            Enchantment enchantment = Registries.ENCHANTMENTS.getValue(id);
            if (enchantment != null) {
                EnchantmentDisplayEntry entry = displayMap.get(enchantment);
                if (entry != null) {
                    text = entry.text;
                    color = entry.color;
                    priority = entry.priority;
                } else {
                    text = id.toString();
                    color = Color.YELLOW;
                    priority = 100;
                }
            } else {
                text = id.toString() + level;
                color = Color.YELLOW;
                priority = 200;
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
}