package com.zergatul.cheatutils.configs;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeaconConfig {

    public boolean enabled;
    public List<BeaconEntry> entries = Collections.synchronizedList(new ArrayList<>());

    public class BeaconEntry {
        public int x;
        public int z;
        public String dimension;
        public Color color;
        public String name;

        public void copyFrom(BeaconEntry other) {
            this.x = other.x;
            this.z = other.z;
            this.color = other.color;
        }
    }
}