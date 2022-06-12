package com.zergatul.cheatutils.controllers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.EntityRendererMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EntityOwnerController {

    public static final EntityOwnerController instance = new EntityOwnerController();

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
                    return Optional.of("Waiting...");
                }
            });

    private EntityOwnerController() {

    }

    @SubscribeEvent
    public void onPostRenderLiving(RenderLivingEvent.Post event) {
        if (!ConfigStore.instance.getConfig().entityOwnerConfig.enabled) {
            return;
        }

        LivingEntity entity = event.getEntity();
        UUID owner = getOwner(entity);
        if (owner == null) {
            return;
        }

        Optional<String> nameOpt = usernameCache.getUnchecked(owner);
        if (nameOpt.isEmpty()) {
            return;
        }

        String name = nameOpt.get();
        PoseStack poseStack = event.getPoseStack();
        EntityRenderDispatcher dispatcher = ((EntityRendererMixinInterface) event.getRenderer()).getDispatcher();

        float height = entity.getBbHeight() + 0.5F;
        int y = 10;
        poseStack.pushPose();
        poseStack.translate(0.0D, height, 0.0D);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = event.getRenderer().getFont();
        float x = (float) (-font.width(name) / 2);

        float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;

        font.drawInBatch(name, x, (float)y, 553648127, false, matrix4f, event.getMultiBufferSource(), true, backgroundColor, event.getPackedLight());
        font.drawInBatch(name, x, (float)y, -1, false, matrix4f, event.getMultiBufferSource(), false, 0, event.getPackedLight());

        poseStack.popPose();
    }

    private UUID getOwner(LivingEntity entity) {
        if (entity instanceof TamableAnimal animal) {
            return animal.getOwnerUUID();
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.getOwnerUUID();
        }
        // fox?
        return null;
    }
}