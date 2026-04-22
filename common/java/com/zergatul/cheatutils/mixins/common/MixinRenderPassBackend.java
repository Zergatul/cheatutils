package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.systems.RenderPassBackend;
import com.zergatul.cheatutils.extensions.RenderPassBackendExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderPassBackend.class)
public abstract class MixinRenderPassBackend implements RenderPassBackendExtension {}