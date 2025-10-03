package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.EntityRenderStateExtension;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class MixinEntityRenderState implements EntityRenderStateExtension {

    @Unique
    private EntityEsp.EntityRenderParameters parameters_CU;

    public EntityEsp.EntityRenderParameters getParameters_CU() {
        return parameters_CU;
    }

    public void setParameters_CU(EntityEsp.EntityRenderParameters parameters) {
        parameters_CU = parameters;
    }
}