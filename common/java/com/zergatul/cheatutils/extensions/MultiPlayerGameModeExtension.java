package com.zergatul.cheatutils.extensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface MultiPlayerGameModeExtension {
    /**
     * A vanilla implementation of the
     * {@code void attack(Player player, Entity entity)}
     * method.<br>
     * This method will NOT trigger automations / events from the regular method.<br>
     * intended to only be used internally to make modules that trigger on beforeAttackMethod
     * Should be used in this format:<br>
     * {@code ((MultiPlayerGameModeExtension) mc.gameMode).attackClone(mc.player, entity);}
     **/
    void attackClone_CU(Player player, Entity entity);
}
