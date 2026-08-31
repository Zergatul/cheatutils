package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.automation.Schematica;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinRenderSectionManager {

    @Redirect(
            method = "onSectionAdded",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z", remap = true),
            remap = false)
    private boolean onCheckSectionEmpty(LevelChunkSection section, int x, int y, int z) {
        Schematica schematica = Schematica.instance;
        return section.hasOnlyAir() && (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(x, y, z));
    }
}