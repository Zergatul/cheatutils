package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.MultiPlayerGameModeExtension;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoStunner implements Module {
    public static final AutoStunner instance = new AutoStunner();
    private final Minecraft mc = Minecraft.getInstance();

    AutoStunner() {
        Events.BeforeAttack.add(this::onBeforeAttack, 1);
    }

    private void onBeforeAttack(BeforeAttackEvent event) {
        if (!ConfigStore.instance.getConfig().autoStunnerConfig.enabled) return;

        if (mc.player == null) return;

        if (mc.hitResult == null) return;

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (!isUsingShield(entity)) return;

        Inventory inventory = mc.player.getInventory();

        int axe = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getTags().anyMatch(tag -> tag == ItemTags.AXES)) {
                axe = i;
                break;
            }
        }
        if (axe == -1) return;

        int prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(axe);

        assert mc.gameMode != null;
        ((MultiPlayerGameModeExtension) mc.gameMode).attackClone_CU(mc.player, entity);

        inventory.setSelectedSlot(prevSelectedSlot);
    }


    private boolean isUsingShield(Entity entity) {
        if (entity instanceof LivingEntity living) {
            if (living.isBlocking()) {
                // Calculate if target is looking at us
                // Shields only block a 180 degree slice of a cylinder
                Vec3 shield = living.calculateViewVector(0.0F, living.getYHeadRot());
                assert mc.player != null;
                Vec3 player = mc.player.position().subtract(living.position());
                player = (new Vec3(player.x, 0D, player.z));
                double dotProduct = player.dot(shield);
                return dotProduct >= 0;
            }
        }
        return false;
    }
}
