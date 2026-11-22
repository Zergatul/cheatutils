package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoFishConfig;
import com.zergatul.cheatutils.mixins.common.accessors.MinecraftAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@NullMarked
public class AutoFish implements Module {

    public static final AutoFish INSTANCE = new AutoFish();

    private static final long PULL_IN_DELAY_MS = 500;

    private final SettingHandle<AutoFishConfig> settings = Settings.get(AutoFishConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private State state = State.NONE;
    private @Nullable FishingHook bobber;
    private long lastPullInTime;

    private AutoFish() {
        Events.BeforeHandlePacketClientThread.add(this::onServerPacket);
        Events.InGameTickEnd.add(this::onClientTickEnd);

        WebApiRegistry.INSTANCE.register("auto-fish", settings, AutoFishConfig.class);
    }

    public SettingHandle<AutoFishConfig> getSettings() {
        return settings;
    }

    private void onClientTickEnd() {
        if (mc.level == null || mc.player == null) {
            state = State.NONE;
            return;
        }

        AutoFishConfig config = settings.get();
        if (!config.enabled()) {
            state = State.NONE;
            return;
        }

        switch (state) {
            case NONE -> {
                state = State.WAITING_FOR_BOBBER;
                lastPullInTime = System.nanoTime();
            }
            case WAITING_FOR_BOBBER -> {
                AABB box = new AABB(
                        mc.player.getX() - 100,
                        mc.player.getY() - 100,
                        mc.player.getZ() - 100,
                        mc.player.getX() + 100,
                        mc.player.getY() + 100,
                        mc.player.getZ() + 100);
                List<FishingHook> bobbers = mc.level.getEntitiesOfClass(FishingHook.class, box);
                bobber = bobbers.stream()
                        .filter(b -> b.getPlayerOwner() == mc.player)
                        .findFirst()
                        .orElse(null);
                if (bobber != null) {
                    state = State.WAITING_FOR_SOUND;
                }
            }
            case WAITING_FOR_SOUND -> {
                if (bobber == null) {
                    throw new IllegalStateException("Bobber is null.");
                }
                if (mc.level.getEntity(bobber.getId()) == null) {
                    // bobber disappeared
                    state = State.WAITING_FOR_BOBBER;
                    break;
                }
                if (config.autoRestartOnIdle() && TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastPullInTime) > config.idleTimeout()) {
                    ((MinecraftAccessor) mc).startUseItem_CU();
                    state = State.DELAY_AFTER_PULL_IN;
                    lastPullInTime = System.nanoTime();
                }
            }
            case PULL_IN -> {
                ((MinecraftAccessor) mc).startUseItem_CU();
                state = State.DELAY_AFTER_PULL_IN;
                lastPullInTime = System.nanoTime();
            }
            case DELAY_AFTER_PULL_IN -> {
                if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastPullInTime) >= PULL_IN_DELAY_MS) {
                    ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
                    if (stack.getItem() == Items.FISHING_ROD) {
                        ((MinecraftAccessor) mc).startUseItem_CU();
                        state = State.WAITING_FOR_BOBBER;
                    }
                }
            }
        }
    }

    private boolean onServerPacket(Packet<?> packet) {
        if (packet instanceof ClientboundSoundPacket soundPacket && soundPacket.getSound().value() == SoundEvents.FISHING_BOBBER_SPLASH) {
            if (state == State.WAITING_FOR_SOUND && bobber != null) {
                if (bobber.distanceToSqr(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()) < 1) {
                    state = State.PULL_IN;
                }
            }
        }
        return true;
    }

    private enum State {
        NONE,
        WAITING_FOR_BOBBER,
        WAITING_FOR_SOUND,
        PULL_IN,
        DELAY_AFTER_PULL_IN,
    }
}