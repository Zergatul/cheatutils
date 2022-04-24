package com.zergatul.cheatutils.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BrandingControl.class)
public class MixinBrandingControl {

    @Shadow(remap = false)
    private static List<String> brandings;

    @Shadow(remap = false)
    private static List<String> brandingsNoMC;

    @Inject(at = @At("HEAD"), method = "Lnet/minecraftforge/internal/BrandingControl;computeBranding()V", cancellable = true, remap = false)
    private static void onComputeBrandings(CallbackInfo info)
    {
        if (brandings == null)
        {
            ImmutableList.Builder<String> brd = ImmutableList.builder();
            brd.add("Forge 111 " + ForgeVersion.getVersion());
            brd.add("Minecraft " + MCPVersion.getMCVersion());
            brd.add("MCP " + MCPVersion.getMCPVersion());
            int tModCount = ModList.get().size();
            brd.add(ForgeI18n.parseMessage("fml.menu.loadingmods", tModCount));
            brandings = brd.build();
            brandingsNoMC = brandings.subList(1, brandings.size());
        }

        info.cancel();
    }

}