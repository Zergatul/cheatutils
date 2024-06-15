package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.controllers.DisconnectController;
import com.zergatul.cheatutils.mixins.common.accessors.MultiPlayerGameModeAccessor;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerApi {

    private final static Minecraft mc = Minecraft.getInstance();

    public TargetApi target = new TargetApi();
    public EffectsApi effects = new EffectsApi();

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

    public boolean isDestroyingBlock() {
        if (mc.gameMode == null) {
            return false;
        }
        MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
        return mode.getIsDestroying_CU();
    }

    public int getDestroyingBlockX() {
        if (mc.gameMode == null) {
            return Integer.MIN_VALUE;
        }
        MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
        return mode.getIsDestroying_CU() ? mode.getDestroyBlockPos_CU().getX() : Integer.MIN_VALUE;
    }

    public int getDestroyingBlockY() {
        if (mc.gameMode == null) {
            return Integer.MIN_VALUE;
        }
        MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
        return mode.getIsDestroying_CU() ? mode.getDestroyBlockPos_CU().getY() : Integer.MIN_VALUE;
    }

    public int getDestroyingBlockZ() {
        if (mc.gameMode == null) {
            return Integer.MIN_VALUE;
        }
        MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
        return mode.getIsDestroying_CU() ? mode.getDestroyBlockPos_CU().getZ() : Integer.MIN_VALUE;
    }

    public double getDestroyingBlockProgress() {
        if (mc.gameMode == null) {
            return Integer.MIN_VALUE;
        }
        MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) mc.gameMode;
        return mode.getIsDestroying_CU() ? mode.getDestroyProgress_CU() : Double.NaN;
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

    @HelpText("types: \"self-attack\", \"invalid-chars\", anything else - normal disconnect.")
    @ApiVisibility(ApiType.DISCONNECT)
    public void disconnect(String type) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(null);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(null);
            default -> DisconnectController.instance.disconnect(null);
        }
    }

    @HelpText("message to be displayed on the disconnect screen")
    @ApiVisibility(ApiType.DISCONNECT)
    public void disconnect(String type, String message) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(message);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(message);
            default -> DisconnectController.instance.disconnect(message);
        }
    }

    @ApiVisibility(ApiType.ACTION)
    public boolean attack(int entityId) {
        if (mc.level == null) {
            return false;
        }
        if (mc.player == null) {
            return false;
        }
        if (mc.gameMode == null) {
            return false;
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity == null) {
            return false;
        }

        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    @HelpText("Returns [0..1]. 0 means attack will do full damage.")
    public double getAttackCooldown() {
        if (mc.player == null) {
            return Double.NaN;
        }
        return 1d - mc.player.getAttackStrengthScale(0);
    }

    public static class TargetApi {

        public boolean hasBlock() {
            if (mc.hitResult == null) {
                return false;
            }

            return mc.hitResult.getType() == HitResult.Type.BLOCK;
        }

        public int getBlockX() {
            if (mc.hitResult instanceof BlockHitResult hitResult) {
                return hitResult.getBlockPos().getX();
            } else {
                return Integer.MIN_VALUE;
            }
        }

        public int getBlockY() {
            if (mc.hitResult instanceof BlockHitResult hitResult) {
                return hitResult.getBlockPos().getY();
            } else {
                return Integer.MIN_VALUE;
            }
        }

        public int getBlockZ() {
            if (mc.hitResult instanceof BlockHitResult hitResult) {
                return hitResult.getBlockPos().getZ();
            } else {
                return Integer.MIN_VALUE;
            }
        }

        public boolean hasEntity() {
            if (mc.hitResult == null) {
                return false;
            }

            return mc.hitResult.getType() == HitResult.Type.ENTITY;
        }

        public int getEntityId() {
            if (mc.hitResult instanceof EntityHitResult hitResult) {
                return hitResult.getEntity().getId();
            } else {
                return Integer.MIN_VALUE;
            }
        }
    }

    public static class EffectsApi {

        @HelpText("If player has no effect, returns 0")
        public int getLevel(String id) {
            if (mc.level == null) {
                return 0;
            }
            if (mc.player == null) {
                return 0;
            }

            HolderLookup<MobEffect> lookup = mc.level.holderLookup(Registries.MOB_EFFECT);
            ResourceLocation location = ResourceLocation.parse(id);
            Holder<MobEffect> holder = lookup.listElements().filter(ref -> ref.key().location().equals(location)).findFirst().orElse(null);
            if (holder == null) {
                return Integer.MIN_VALUE;
            }

            MobEffectInstance instance = mc.player.getActiveEffectsMap().get(holder);
            if (instance == null) {
                return 0;
            }

            return instance.getAmplifier() + 1;
        }

        @HelpText("If player has no effect, returns 0")
        public double getDuration(String id) {
            if (mc.level == null) {
                return 0;
            }
            if (mc.player == null) {
                return 0;
            }

            HolderLookup<MobEffect> lookup = mc.level.holderLookup(Registries.MOB_EFFECT);
            ResourceLocation location = ResourceLocation.parse(id);
            Holder<MobEffect> holder = lookup.listElements().filter(ref -> ref.key().location().equals(location)).findFirst().orElse(null);
            if (holder == null) {
                return -1;
            }

            MobEffectInstance instance = mc.player.getActiveEffectsMap().get(holder);
            if (instance == null) {
                return 0;
            }

            return instance.getDuration() / 20d;
        }
    }
}