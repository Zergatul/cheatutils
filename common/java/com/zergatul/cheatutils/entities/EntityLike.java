package com.zergatul.cheatutils.entities;

import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.mixins.common.accessors.ProjectileAccessor;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class EntityLike {

    private static final List<EquipmentSlot> EQUIPMENT_ORDER = List.of(
            EquipmentSlot.MAINHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.BODY,
            EquipmentSlot.SADDLE,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND);

    public abstract Vec3 getPosition();
    public abstract double getHeight();
    public abstract boolean hasHealth();
    public abstract int getHealth();
    public abstract int getAbsorption();
    public abstract UUID getOwner();
    public abstract void collectEquipment(List<ItemStack> items);
    public abstract LivingEntity getEquipmentOwner();
    public abstract boolean hasCustomName();
    public abstract Component getDisplayName();

    public StylizedText getTitleSuffix() {
        return null;
    }

    public static EntityLike fromEntity(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return new LivingEntityWrapper(living);
        } else {
            return new EntityWrapper(entity);
        }
    }

    public static EntityLike asDisconnectedPlayer(RemotePlayer player) {
        return new DisconnectedPlayerWrapper(player);
    }

    private static class EntityWrapper extends EntityLike {

        private final Entity entity;

        public EntityWrapper(Entity entity) {
            this.entity = entity;
        }

        @Override
        public Vec3 getPosition() {
            return entity.position();
        }

        @Override
        public double getHeight() {
            return entity.getBbHeight();
        }

        @Override
        public boolean hasHealth() {
            return false;
        }

        @Override
        public int getHealth() {
            return 0;
        }

        @Override
        public int getAbsorption() {
            return 0;
        }

        @Override
        public UUID getOwner() {
            if (entity instanceof Projectile projectile) {
                ProjectileAccessor projectileMixin = (ProjectileAccessor) projectile;
                EntityReference<Entity> reference = projectileMixin.getOwner_CU();
                return reference != null ? reference.getUUID() : null;
            }
            return null;
        }

        @Override
        public void collectEquipment(List<ItemStack> items) {
            items.clear();
        }

        public LivingEntity getEquipmentOwner() {
            return null;
        }

        @Override
        public boolean hasCustomName() {
            return entity.hasCustomName();
        }

        @Override
        public Component getDisplayName() {
            return entity.getDisplayName();
        }
    }

    private static class LivingEntityWrapper extends EntityLike {

        private final LivingEntity entity;

        public LivingEntityWrapper(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public Vec3 getPosition() {
            return entity.position();
        }

        @Override
        public double getHeight() {
            return entity.getBbHeight();
        }

        @Override
        public boolean hasHealth() {
            return true;
        }

        @Override
        public int getHealth() {
            return (int) entity.getHealth();
        }

        @Override
        public int getAbsorption() {
            return (int) entity.getAbsorptionAmount();
        }

        @Override
        public UUID getOwner() {
            if (entity instanceof TamableAnimal animal) {
                EntityReference<LivingEntity> reference = animal.getOwnerReference();
                return reference != null ? reference.getUUID() : null;
            }
            if (entity instanceof AbstractHorse horse) {
                EntityReference<LivingEntity> reference = horse.getOwnerReference();
                return reference != null ? reference.getUUID() : null;
            }
            return null;
        }

        @Override
        public void collectEquipment(List<ItemStack> items) {
            items.clear();
            for (EquipmentSlot slot : EQUIPMENT_ORDER) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (!itemStack.isEmpty()) {
                    items.add(itemStack);
                }
            }
        }

        @Override
        public LivingEntity getEquipmentOwner() {
            return entity;
        }

        @Override
        public boolean hasCustomName() {
            return entity.hasCustomName() || entity instanceof Player;
        }

        @Override
        public Component getDisplayName() {
            return entity.getDisplayName();
        }
    }

    private static class DisconnectedPlayerWrapper extends EntityLike {

        private final Vec3 position;
        private final double height;
        private final int hp;
        private final int absorption;
        private final EntityEquipment equipment;
        private final Component name;
        private final long time;

        public DisconnectedPlayerWrapper(RemotePlayer player) {
            this.position = player.position();
            this.height = player.getBbHeight();
            this.hp = (int) player.getHealth();
            this.absorption = (int) player.getAbsorptionAmount();
            this.equipment = new EntityEquipment();
            for (var slot : EquipmentSlot.values()) {
                this.equipment.set(slot, player.getItemBySlot(slot));
            }
            this.name = player.getDisplayName();
            this.time = System.nanoTime();
        }

        @Override
        public Vec3 getPosition() {
            return position;
        }

        @Override
        public double getHeight() {
            return height;
        }

        @Override
        public boolean hasHealth() {
            return true;
        }

        @Override
        public int getHealth() {
            return hp;
        }

        @Override
        public int getAbsorption() {
            return absorption;
        }

        @Override
        public UUID getOwner() {
            return null;
        }

        @Override
        public void collectEquipment(List<ItemStack> items) {
            items.clear();
            for (EquipmentSlot slot : EQUIPMENT_ORDER) {
                ItemStack itemStack = equipment.get(slot);
                if (!itemStack.isEmpty()) {
                    items.add(itemStack);
                }
            }
        }

        @Override
        public LivingEntity getEquipmentOwner() {
            return null;
        }

        @Override
        public boolean hasCustomName() {
            return true;
        }

        @Override
        public Component getDisplayName() {
            return name;
        }

        @Override
        public StylizedText getTitleSuffix() {
            long seconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
            String value;
            if (seconds < 1) {
                value = " (just now)";
            } else if (seconds == 1) {
                value = " (second ago)";
            } else if (seconds < 60) {
                value = String.format(" (%s seconds ago)", seconds);
            } else if (seconds < 60 * 2) {
                value = " (minute ago)";
            } else {
                value = String.format(" (%s minutes ago)", seconds / 60);
            }
            return StylizedText.of(value);
        }
    }
}