package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.zergatul.cheatutils.mixins.common.accessors.RenderSetupAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.RenderTypeAccessor;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public class RenderTypeHelper {

    public static Optional<Identifier> getTextureLocation(RenderType type) {
        if (type.primitiveTopology() != PrimitiveTopology.QUADS) {
            return Optional.empty();
        }

        RenderSetup setup = ((RenderTypeAccessor) type).getState_CU();
        Map<String, RenderSetup.TextureBinding> textures = ((RenderSetupAccessor) (Object) setup).getTextures_CU();
        RenderSetup.TextureBinding binding = textures.get("Sampler0");
        if (binding == null) {
            return Optional.empty();
        }

        return Optional.of(binding.location());
    }
}