package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.automation.Schematica;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelSlice.class)
public abstract class MixinLevelSlice {

//    @Unique
//    private static SectionPos currentSectionPos_CU;
//
//    @Inject(at = @At("HEAD"), method = "prepare")
//    private static void onBeforePrepare(Level level, SectionPos pos, ClonedChunkSectionCache cache, CallbackInfoReturnable<ChunkRenderContext> info) {
//        currentSectionPos_CU = pos;
//    }
//
//    @Redirect(method = "prepare", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
//    private static boolean onModifyHasOnlyAir(LevelChunkSection section) {
//        if (!section.hasOnlyAir()) {
//            return false;
//        }
//
//        return !Schematica.instance.hasBlocksAtSection(currentSectionPos_CU.getX(), currentSectionPos_CU.getY(), currentSectionPos_CU.getZ());
//    }
}