package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.collections.TaggedArrayList;
import com.zergatul.cheatutils.extensions.GuiGraphicsExtractorExtension;
import com.zergatul.cheatutils.extensions.GuiRenderStateExtension;
import com.zergatul.cheatutils.font.FontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.render.GuiCustomTextRenderState;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphicsExtractor.class)
public abstract class MixinGuiGraphicsExtractor implements GuiGraphicsExtractorExtension {

    @Shadow
    @Final
    public GuiRenderState guiRenderState;

    @Unique
    private TaggedArrayList<Component, ItemStack> storedComponents_CU;

    @Override
    public void customText_CU(FontRenderer font, StylizedText text, int x, int y) {
        ((GuiRenderStateExtension) this.guiRenderState).addCustomText_CU(new GuiCustomTextRenderState(font, text, x, y));
    }

    @Inject(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"))
    private void onBeforeSetTooltipForNextFrame(
            Font font,
            List<Component> texts,
            Optional<TooltipComponent> optionalImage,
            int x, int y,
            @Nullable Identifier style,
            CallbackInfo info
    ) {
        if (texts instanceof TaggedArrayList<?,?>) {
            storedComponents_CU = (TaggedArrayList<Component, ItemStack>) texts;
        }
    }

    @ModifyArg(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V"))
    private List<ClientTooltipComponent> onModifyComponentsList(List<ClientTooltipComponent> components) {
        if (storedComponents_CU != null) {
            return new TaggedArrayList<>(components, storedComponents_CU.getTag());
        } else {
            return components;
        }
    }

    @Inject(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At("TAIL"))
    private void onAfterSetTooltipForNextFrame(
            Font font,
            List<Component> texts,
            Optional<TooltipComponent> optionalImage,
            int x, int y,
            @Nullable Identifier style,
            CallbackInfo info
    ) {
        storedComponents_CU = null;
    }
}