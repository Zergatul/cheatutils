package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.CameraMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Camera.class)
public class MixinCamera implements CameraMixinInterface {

    @Shadow(aliases = "Lnet/minecraft/client/Camera;entity:Lnet/minecraft/world/entity/Entity;")
    private Entity entity;

    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}