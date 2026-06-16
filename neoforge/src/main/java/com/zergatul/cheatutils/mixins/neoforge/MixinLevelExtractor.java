package com.zergatul.cheatutils.mixins.neoforge;

import com.zergatul.cheatutils.entities.FakePlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelExtractor.class)
public abstract class MixinLevelExtractor {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract EntityRenderState extractEntity(Entity entity, float partialTickTime);

    @Inject(at = @At("TAIL"), method = "extractVisibleEntities")
    private void onAfterExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState output, CallbackInfo info) {
        List<FakePlayer> fakes = FakePlayer.getList();
        if (fakes.isEmpty()) {
            return;
        }

        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }

        for (FakePlayer fake : fakes) {
            if (fake.distanceToSqr(player) > 1) {
                EntityRenderState entityRenderState = this.extractEntity(fake, 1);
                output.entityRenderStates.add(entityRenderState);
            }
        }
    }
}