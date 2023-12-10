package com.zergatul.cheatutils.scripting.modules;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.mixins.common.accessors.KeyMappingAccessor;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeysApi {

    private final Minecraft mc = Minecraft.getInstance();

    public KeyEntry up = new KeyEntry(mc.options.keyUp);
    public KeyEntry down = new KeyEntry(mc.options.keyDown);
    public KeyEntry left = new KeyEntry(mc.options.keyLeft);
    public KeyEntry right = new KeyEntry(mc.options.keyRight);
    public KeyEntry jump = new KeyEntry(mc.options.keyJump);
    public KeyEntry sneak = new KeyEntry(mc.options.keyShift);
    public KeyEntry sprint = new KeyEntry(mc.options.keySprint);
    public KeyEntry drop = new KeyEntry(mc.options.keyDrop);
    public KeyEntry attack = new KeyEntry(mc.options.keyAttack);
    public KeyEntry use = new KeyEntry(mc.options.keyUse);

    public static class KeyEntry {

        private final KeyMapping mapping;

        public KeyEntry(KeyMapping mapping) {
            this.mapping = mapping;
        }

        @ApiVisibility(ApiType.ACTION)
        public void click() {
            KeyMapping.click(getKey());
        }

        @ApiVisibility(ApiType.ACTION)
        public void setDown(boolean state) {
            KeyMapping.set(getKey(), state);
        }

        public boolean isDown() {
            return mapping.isDown();
        }

        private InputConstants.Key getKey() {
            return ((KeyMappingAccessor) mapping).getKey_CU();
        }
    }
}