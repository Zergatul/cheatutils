package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.compatibility.WrappedRenderType;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeRenderTypeAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeStateAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.MultiTextureStateShardAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.TextureStateShardAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class RenderTypeTextureResolver {

    private static final Map<RenderType, Optional<ResourceLocation>> cache = Collections.synchronizedMap(new IdentityHashMap<>());

    private RenderTypeTextureResolver() {}

    public static ResourceLocation getTexture(RenderType renderType) {
        return cache.computeIfAbsent(renderType, RenderTypeTextureResolver::inspectTexture).orElse(null);
    }

    private static Optional<ResourceLocation> inspectTexture(RenderType renderType) {
        // Iris compatibility
        if (renderType instanceof WrappedRenderType wrapped) {
            renderType = wrapped.unwrap();
        }

        if (renderType instanceof CompositeRenderTypeAccessor compositeRenderType) {
            RenderType.CompositeState compositeState = compositeRenderType.getState_CU();
            CompositeStateAccessor compositeStateAccessor = (CompositeStateAccessor) (Object) compositeState;
            RenderStateShard.EmptyTextureStateShard textureState = compositeStateAccessor.getTextureState_CU();
            if (textureState instanceof TextureStateShardAccessor accessor) {
                return accessor.getTexture_CU();
            }
            if (textureState instanceof MultiTextureStateShardAccessor accessor) {
                return accessor.getCutoutTexture_CU();
            }
        }

        return Optional.empty();
    }
}