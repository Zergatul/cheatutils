package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.ui.CustomToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.client.gui.toasts.GuiToast$ToastInstance")
public abstract class MixinGuiToastToastInstance {

    @Shadow
    @Final
    private IToast toast;

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 160.0F), require = 1)
    private float onGetToastWidth(float width) {
        return this.toast instanceof CustomToast ? ((CustomToast) this.toast).getWidth() : width;
    }
}