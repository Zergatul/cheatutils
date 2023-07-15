package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.PerformanceConfig;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    public abstract boolean isWindowActive();

    @Shadow
    protected abstract void continueAttack(boolean p_91387_);

    @Inject(at = @At("HEAD"), method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        for (EntityTracerConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.enabled && config.isValidEntity(entity) && config.glow && entity.distanceToSqr(player) < config.getGlowMaxDistanceSqr()) {
                info.setReturnValue(true);
                info.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "close()V")
    private void onClose(CallbackInfo info) {
        ConfigStore.instance.onClose();
    }

    @Inject(at = @At("HEAD"), method = "handleKeybinds()V")
    private void onBeforeHandleKeyBindings(CallbackInfo info) {
        Events.BeforeHandleKeyBindings.trigger();
    }

    @Inject(at = @At("TAIL"), method = "handleKeybinds()V")
    private void onAfterHandleKeyBindings(CallbackInfo info) {
        Events.AfterHandleKeyBindings.trigger();
    }

    @Inject(at = @At("RETURN"), method = "createTitle()Ljava/lang/String;", cancellable = true)
    private void onCreateTitle(CallbackInfoReturnable<String> info) {
        if (ConfigStore.instance.getConfig().userNameConfig.showNameInTitle) {
            info.setReturnValue(Minecraft.getInstance().getUser().getName() + " - " + info.getReturnValue());
        }
    }

    @Inject(at = @At("HEAD"), method = "getFramerateLimit()I", cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        PerformanceConfig config = ConfigStore.instance.getConfig().performanceConfig;
        if (config.limitBackgroundWindowFps && !isWindowActive()) {
            info.setReturnValue(config.backgroundWindowFps);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    private void onBeforeTick(CallbackInfo info) {
        Events.ClientTickStart.trigger();
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void onAfterTick(CallbackInfo info) {
        Events.ClientTickEnd.trigger();
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER),
            method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void onPlayerLoggingOut(Screen screen, CallbackInfo info) {
        Events.ClientPlayerLoggingOut.trigger();
    }

    @Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    private void onSetLevel(ClientLevel level, CallbackInfo info) {
        if (this.level != null) {
            Events.WorldUnload.trigger();
        }
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateScreenAndTick(Lnet/minecraft/client/gui/screens/Screen;)V", shift = At.Shift.AFTER),
            method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void onClearLevel(Screen screen, CallbackInfo info) {
        if (this.level != null) {
            Events.WorldUnload.trigger();
        }
    }

    @Redirect(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
    private void onShouldContinueAttack(Minecraft instance, boolean value) {
        if (VillagerRoller.instance.isBreakingBlock()) {
            return;
        }

        this.continueAttack(value);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;passEvents:Z", opcode = Opcodes.GETFIELD))
    private boolean onTickScreenPassEvents(Screen screen) {
        if (InvMove.instance.shouldPassEvents(screen)) {
            return true;
        } else {
            return screen.passEvents;
        }
    }
}