package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.GuiRenderStateNodeExtension;
import com.zergatul.cheatutils.render.GuiCustomTextRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiRenderState.Node.class)
public abstract class MixinGuiRenderStateNode implements GuiRenderStateNodeExtension {

    @Unique
    private @Nullable List<GuiCustomTextRenderState> customTextStates_CU;

    @Override
    public void addCustomText_CU(GuiCustomTextRenderState state) {
        if (this.customTextStates_CU == null) {
            this.customTextStates_CU = new ArrayList<>();
        }

        this.customTextStates_CU.add(state);
    }

    @Override
    public @Nullable List<GuiCustomTextRenderState> getCustomTextStates_CU() {
        return this.customTextStates_CU;
    }
}