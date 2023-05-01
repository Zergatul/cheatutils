package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoAttackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.wrappers.AttackRange;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoAttack implements Module {

    public static final AutoAttack instance = new AutoAttack();

    private final Minecraft mc = Minecraft.getInstance();

    private AutoAttack() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        AutoAttackConfig config = ConfigStore.instance.getConfig().autoAttackConfig;
        if (!config.enabled) {
            return;
        }

        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        if (mc.hitResult == null) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        if (mc.player.getAttackStrengthScale((float) -config.extraTicks) != 1) {
            return;
        }

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (AttackRange.canHit(entity)) {
            mc.gameMode.attack(mc.player, entity);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}