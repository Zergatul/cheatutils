package com.zergatul.cheatutils.interfaces;

import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.entity.Entity;

public interface ClientWorldMixinInterface {
    ClientEntityManager<Entity> getEntityManager();
}