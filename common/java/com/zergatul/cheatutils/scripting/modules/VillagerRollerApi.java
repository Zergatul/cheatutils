package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

@SuppressWarnings("unused")
public class VillagerRollerApi {

    public boolean isActive() {
        return VillagerRoller.INSTANCE.isActive();
    }

    public String getStopReason() {
        String reason = VillagerRoller.INSTANCE.getStopReason();
        return reason == null ? "" : reason;
    }

    public void resetStopReason() {
        VillagerRoller.INSTANCE.resetStopReason();
    }

    public String getState() {
        return VillagerRoller.INSTANCE.getState();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void start() {
        VillagerRoller.INSTANCE.start();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void stop() {
        VillagerRoller.INSTANCE.stop();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void toggle() {
        VillagerRoller.INSTANCE.toggle();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public String getEnchantmentId() {
        return VillagerRoller.INSTANCE.getEnchantmentId();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public String getEnchantmentName() {
        return VillagerRoller.INSTANCE.getEnchantmentName();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getLevel() {
        return VillagerRoller.INSTANCE.getLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMaxLevel() {
        return VillagerRoller.INSTANCE.getMaxLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isMaxLevel() {
        return VillagerRoller.INSTANCE.getLevel() == VillagerRoller.INSTANCE.getMaxLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getPrice() {
        return VillagerRoller.INSTANCE.getPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMinPrice() {
        return VillagerRoller.INSTANCE.getMinPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMaxPrice() {
        return VillagerRoller.INSTANCE.getMaxPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isBestPrice() {
        return VillagerRoller.INSTANCE.getPrice() == VillagerRoller.INSTANCE.getMinPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isCurse() {
        return VillagerRoller.INSTANCE.isCurse();
    }
}