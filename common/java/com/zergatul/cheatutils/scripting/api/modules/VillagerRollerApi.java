package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class VillagerRollerApi {

    public boolean isActive() {
        return VillagerRoller.instance.isActive();
    }

    public String getStopReason() {
        String reason = VillagerRoller.instance.getStopReason();
        return reason == null ? "" : reason;
    }

    public void resetStopReason() {
        VillagerRoller.instance.resetStopReason();
    }

    public String getState() {
        return VillagerRoller.instance.getState();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void start() {
        VillagerRoller.instance.start();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void stop() {
        VillagerRoller.instance.stop();
    }

    @ApiVisibility({ ApiType.UPDATE, ApiType.VILLAGER_ROLLER })
    public void toggle() {
        VillagerRoller.instance.toggle();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public String getEnchantmentId() {
        return VillagerRoller.instance.getEnchantmentId();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public String getEnchantmentName() {
        return VillagerRoller.instance.getEnchantmentName();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getLevel() {
        return VillagerRoller.instance.getLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMaxLevel() {
        return VillagerRoller.instance.getMaxLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isMaxLevel() {
        return VillagerRoller.instance.getLevel() == VillagerRoller.instance.getMaxLevel();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getPrice() {
        return VillagerRoller.instance.getPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMinPrice() {
        return VillagerRoller.instance.getMinPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public int getMaxPrice() {
        return VillagerRoller.instance.getMaxPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isBestPrice() {
        return VillagerRoller.instance.getPrice() == VillagerRoller.instance.getMinPrice();
    }

    @ApiVisibility(ApiType.VILLAGER_ROLLER)
    public boolean isCurse() {
        return VillagerRoller.instance.isCurse();
    }
}