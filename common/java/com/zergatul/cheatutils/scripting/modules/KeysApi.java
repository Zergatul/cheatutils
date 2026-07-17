package com.zergatul.cheatutils.scripting.modules;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.mixins.common.accessors.KeyMappingAccessor;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class KeysApi {

    private final Minecraft mc = Minecraft.getInstance();

    public final KeyEntry up = new KeyEntry(mc.options.keyUp);
    public final KeyEntry down = new KeyEntry(mc.options.keyDown);
    public final KeyEntry left = new KeyEntry(mc.options.keyLeft);
    public final KeyEntry right = new KeyEntry(mc.options.keyRight);
    public final KeyEntry jump = new KeyEntry(mc.options.keyJump);
    public final KeyEntry sneak = new KeyEntry(mc.options.keyShift);
    public final KeyEntry sprint = new KeyEntry(mc.options.keySprint);
    public final KeyEntry drop = new KeyEntry(mc.options.keyDrop);
    public final KeyEntry attack = new KeyEntry(mc.options.keyAttack);
    public final KeyEntry use = new KeyEntry(mc.options.keyUse);

    public static class KeyEntry {

        private final KeyMapping mapping;

        private KeyEntry(KeyMapping mapping) {
            this.mapping = mapping;
        }

        @MethodDescription("""
                Emulates click
                """)
        @ApiVisibility(ApiType.ACTION)
        public void click() {
            KeyMapping.click(getKey());
        }

        @MethodDescription("""
                Use this to hold button for some amount of time
                """)
        @ApiVisibility(ApiType.ACTION)
        public void setDown(boolean state) {
            KeyMapping.set(getKey(), state);
        }

        @MethodDescription("""
                Returns true is button is pressed
                """)
        public boolean isDown() {
            return mapping.isDown();
        }

        private InputConstants.Key getKey() {
            return ((KeyMappingAccessor) mapping).getKey_CU();
        }
    }
}