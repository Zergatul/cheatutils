package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Invoker("startUseItem")
    void startUseItem_CU();

    /**
     * This method will trigger automations / events from the regular method.<br>
     * intended to only be used internally to make modules that trigger on beforeAttackStartMethod.<br>
     * Should be used in this format:<br>
     * {@code ((MinecraftExtension) mc).runStartAttack_CU();}
     **/
    @Invoker("startAttack")
    boolean startAttack_CU();

    @Accessor("clientTickCount")
    long getClientTickCount_CU();
}