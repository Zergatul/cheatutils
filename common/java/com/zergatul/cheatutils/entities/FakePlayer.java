package com.zergatul.cheatutils.entities;

import com.zergatul.cheatutils.mixins.common.accessors.LivingEntityAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.WalkAnimationStateAccessor;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FakePlayer extends RemotePlayer {

    private static final AtomicInteger nextId = new AtomicInteger(-1);
    private static final List<FakePlayer> list = new ArrayList<>();

    private final ClientAvatarState state;
    private final ItemStack mainHand;
    private final ItemStack offHand;
    private final ItemStack feet;
    private final ItemStack legs;
    private final ItemStack chest;
    private final ItemStack head;
    private final ItemStack body;
    private final LivingEntity.@Nullable SwingDescription currentSwing;
    private final float swingAnimation;

    public FakePlayer(LocalPlayer player) {
        super((ClientLevel) player.level(), player.getGameProfile());
        this.setId(nextId.getAndDecrement());
        list.add(this);

        this.state = new SnapshotClientAvatarState(player.avatarState());

        this.currentSwing = player.getCurrentSwing();
        this.swingAnimation = player.getSwingAnimation(1);
        this.fallFlyTicks = player.getFallFlyingTicks();
        this.hurtDuration = player.hurtDuration;
        this.hurtTime = player.hurtTime;
        this.hurtDir = player.getHurtDir();
        //this.lastHurt = player.lasthurt
        ((LivingEntityAccessor) this).setSwimAmount_CU(((LivingEntityAccessor) player).getSwimAmount_CU());
        ((LivingEntityAccessor) this).setSwimAmount0_CU(((LivingEntityAccessor) player).getSwimAmount_CU());
        this.useItem = player.getUseItem().copy();
        this.xxa = player.xxa;
        this.yya = player.yya;
        this.zza = player.zza;

        ((WalkAnimationStateAccessor) this.walkAnimation).setSpeedOld_CU(player.walkAnimation.speed());
        this.walkAnimation.setSpeed(player.walkAnimation.speed());
        ((WalkAnimationStateAccessor) this.walkAnimation).setPosition_CU(player.walkAnimation.position());

        this.setPos(player.getPosition(1));
        this.setRot(player.getYRot(), player.getXRot());
        this.setOldPosAndRot();

        this.yBodyRot = this.yBodyRotO = player.yBodyRot;
        this.yHeadRot = this.yHeadRotO = player.yHeadRot;

        this.mainHand = player.getItemBySlot(EquipmentSlot.MAINHAND).copy();
        this.offHand = player.getItemBySlot(EquipmentSlot.OFFHAND).copy();
        this.head = player.getItemBySlot(EquipmentSlot.HEAD).copy();
        this.chest = player.getItemBySlot(EquipmentSlot.CHEST).copy();
        this.legs = player.getItemBySlot(EquipmentSlot.LEGS).copy();
        this.feet = player.getItemBySlot(EquipmentSlot.FEET).copy();
        this.body = player.getItemBySlot(EquipmentSlot.BODY).copy();

        this.entityData.set(DATA_SHARED_FLAGS_ID, player.getEntityData().get(DATA_SHARED_FLAGS_ID));
        this.entityData.set(DATA_POSE, player.getEntityData().get(DATA_POSE));
    }

    @Override
    public @NotNull ClientAvatarState avatarState() {
        return state;
    }

    @Override
    public LivingEntity.@Nullable SwingDescription getCurrentSwing() {
        return currentSwing;
    }

    @Override
    public float getSwingAnimation(float partialTicks) {
        return swingAnimation;
    }

    @Override
    public boolean isSwinging() {
        return currentSwing != null;
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> mainHand;
            case OFFHAND -> offHand;
            case HEAD -> head;
            case CHEST -> chest;
            case LEGS -> legs;
            case FEET -> feet;
            case BODY -> body;
            case SADDLE -> ItemStack.EMPTY;
        };
    }

    @Override
    public void remove(RemovalReason reason) {
        list.remove(this);
    }

    public static List<FakePlayer> getList() {
        return list;
    }

    private static class SnapshotClientAvatarState extends ClientAvatarState {

        private final float bob;
        private final double cloakX;
        private final double cloakY;
        private final double cloakZ;
        private final float walkDistance;
        private final float backwardWalkDistance;

        public SnapshotClientAvatarState(ClientAvatarState other) {
            this.bob = other.getInterpolatedBob(1);
            this.cloakX = other.getInterpolatedCloakX(1);
            this.cloakY = other.getInterpolatedCloakY(1);
            this.cloakZ = other.getInterpolatedCloakZ(1);
            this.walkDistance = other.getInterpolatedWalkDistance(1);
            this.backwardWalkDistance = other.getBackwardsInterpolatedWalkDistance(1);
        }

        @Override
        public float getInterpolatedBob(float f) {
            return bob;
        }

        @Override
        public double getInterpolatedCloakX(float f) {
            return cloakX;
        }

        @Override
        public double getInterpolatedCloakY(float f) {
            return cloakY;
        }

        @Override
        public double getInterpolatedCloakZ(float f) {
            return cloakZ;
        }

        @Override
        public float getInterpolatedWalkDistance(float f) {
            return walkDistance;
        }

        @Override
        public float getBackwardsInterpolatedWalkDistance(float f) {
            return backwardWalkDistance;
        }
    }
}