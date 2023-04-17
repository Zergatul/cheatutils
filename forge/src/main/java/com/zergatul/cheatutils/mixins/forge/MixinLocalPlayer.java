package com.zergatul.cheatutils.mixins.forge;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    public MixinLocalPlayer(ClientLevel p_250460_, GameProfile p_249912_) {
        super(p_250460_, p_249912_);
    }

    @Override
    public double getReachDistance() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideReachDistance) {
            return config.reachDistance;
        } else {
            return super.getReachDistance();
        }
    }

    @Override
    public double getAttackRange() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideAttackRange) {
            return config.attackRange;
        } else {
            return super.getAttackRange();
        }
    }
}