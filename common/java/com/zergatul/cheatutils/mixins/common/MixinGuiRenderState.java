package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.GuiRenderStateExtension;
import com.zergatul.cheatutils.extensions.GuiRenderStateNodeExtension;
import com.zergatul.cheatutils.render.CustomTextRenderState;
import com.zergatul.cheatutils.render.GuiCustomTextRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.ScreenArea;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;

@Mixin(GuiRenderState.class)
public abstract class MixinGuiRenderState implements GuiRenderStateExtension {

    @Shadow
    private GuiRenderState.Node current;

    @Shadow
    protected abstract boolean findAppropriateNode(ScreenArea screenArea);

    @Shadow
    protected abstract void addDebugRectangleIfEnabled(@Nullable ScreenRectangle bounds);

    @Shadow
    protected abstract void traverse(Consumer<GuiRenderState.Node> consumer, GuiRenderState.TraverseRange range);

    @Override
    public void addCustomText_CU(GuiCustomTextRenderState state) {
        if (this.findAppropriateNode(state)) {
            ((GuiRenderStateNodeExtension) this.current).addCustomText_CU(state);
            this.addDebugRectangleIfEnabled(state.bounds());
        }
    }

    @Override
    public void addCustomTextToCurrentLayer_CU(CustomTextRenderState state) {
        ((GuiRenderStateNodeExtension) this.current).addCustomText_CU(state);
    }

    @Override
    public void forEachCustomText_CU(Consumer<GuiCustomTextRenderState> consumer) {
        GuiRenderState.Node currentBackup = this.current;
        this.traverse(node -> {
            List<GuiCustomTextRenderState> states = ((GuiRenderStateNodeExtension) node).getCustomTextStates_CU();
            if (states != null) {
                for (GuiCustomTextRenderState state : states) {
                    this.current = node;
                    consumer.accept(state);
                }
            }
        }, GuiRenderState.TraverseRange.ALL);
        this.current = currentBackup;
    }
}