package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.extensions.ParametrizedSubmit;
import com.zergatul.cheatutils.helpers.MixinLevelRendererHelper;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubmitNodeStorage.ItemSubmit.class)
public abstract class MixinSubmitNodeStorageItemSubmit implements ParametrizedSubmit {

    @Unique
    private EntityEsp.EntityRenderParameters parameters_CU;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onConstructor(
            PoseStack.Pose pose,
            ItemDisplayContext context,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            int[] tintLayers,
            List<BakedQuad> quads,
            RenderType renderType,
            ItemStackRenderState.FoilType foilType,
            CallbackInfo info
    ) {
        parameters_CU = MixinLevelRendererHelper.CURRENT_SUBMIT_ENTITY_RENDER_PARAMETERS;
    }

    public EntityEsp.EntityRenderParameters getParameters_CU() {
        return parameters_CU;
    }
}