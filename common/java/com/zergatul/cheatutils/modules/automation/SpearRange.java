package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import static net.minecraft.core.component.DataComponents.ATTACK_RANGE;

public class SpearRange implements Module {
    public static final SpearRange instance = new SpearRange();
    private final Minecraft mc = Minecraft.getInstance();
    private int prevSelectedSlot = -1;


    private SpearRange() {
        Events.BeforeStartAttack.add(this::onBeforeStartAttack, 0);
        Events.AfterStartAttack.add(this::onAfterStartAttack, 0);
    }

    public void onBeforeStartAttack() {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        prevSelectedSlot = -1;
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;
        if (mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.MISS) return;

        Inventory inventory = mc.player.getInventory();
        int spear = spearPos(inventory);
        if (spear == -1) return;
        if (spear == inventory.getSelectedSlot()) return;

        //Check for valid target before executing
        if (!ConfigStore.instance.getConfig().spearRangeConfig.lungeWithoutTarget) {
            if (!hasValidTarget(inventory.getItem(spear))) {
                return;
            }
        }

        prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(spear);
    }

    public void onAfterStartAttack() {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        if (mc.player == null) return;
        if (prevSelectedSlot == -1) return;
        mc.player.getInventory().setSelectedSlot(prevSelectedSlot);
        prevSelectedSlot = -1;
    }

    /**
     *
     * @param inventory player inventory
     * @return returns position of spear in the hotbar. If no spear is present, returns -1
     */
    private int spearPos(Inventory inventory) {
        int spear = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getComponents().stream().anyMatch(component -> component.type() == ATTACK_RANGE)) {
                spear = i;
                break;
            }
        }
        return spear;
    }

    private boolean hasValidTarget(ItemStack spear) {
        assert mc.level != null;
        assert mc.player != null;
        double maxDist = spear.getComponents().get(ATTACK_RANGE).effectiveMaxRange(mc.player);
        double maxDistSqr = maxDist * maxDist;

        Vec3 origin = mc.player.getEyePosition();
        Vec3 point = mc.player.getLookAngle().scale(maxDist).add(origin);

        ClipContext levelClip = new ClipContext(
                origin,
                point,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        );

        BlockHitResult blockHit = mc.level.clip(levelClip);

        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistSqr = blockHit.getLocation().distanceToSqr(origin);//Limit max range after
        }                                                             //to compare with entity hit test

        AABB searchBox = mc.player.getBoundingBox().expandTowards(point.subtract(origin)).inflate(1);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                mc.player,
                origin,
                point,
                searchBox,
                e -> e != mc.player && e != mc.player.getVehicle(),
                maxDistSqr
        );

        if (entityHit == null) {
            return false;
        }

        if (entityHit.getType() != EntityHitResult.Type.MISS) {
            //Only accept point if block collision is further than entity collision
            return true;
        }
        return false;
    }

}
