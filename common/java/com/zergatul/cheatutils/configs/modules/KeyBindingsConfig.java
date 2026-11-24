package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

import java.util.HashSet;
import java.util.Set;

public record KeyBindingsConfig(
        ImmutableList<KeyBindingEntry> entries
) implements ImmutableConfig, ModuleStateProvider {

    public KeyBindingsConfig {
        if (entries == null) {
            entries = ImmutableList.empty();
        }

        entries = renameDuplicates(entries);
        entries = removeMultiAssignments(entries);
    }

    public static KeyBindingsConfig defaults() {
        return new Builder()
                .entries(ImmutableList.<KeyBindingEntry>empty()
                        .add(new KeyBindingEntry("Toggle ESP", "esp.toggle();", 0))
                        .add(new KeyBindingEntry("Toggle FreeCam", "freeCam.toggle();", 1)))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return !entries.isEmpty();
    }

    private static ImmutableList<KeyBindingEntry> renameDuplicates(ImmutableList<KeyBindingEntry> entries) {
        Set<String> used = new HashSet<>();
        for (int i1 = 0; i1 < entries.size(); i1++) {
            KeyBindingEntry entry1 = entries.get(i1);
            if (!used.add(entry1.name())) {
                ImmutableList<KeyBindingEntry> result = new ImmutableList<>();
                for (int i2 = 0; i2 < i1; i2++) {
                    result = result.add(entries.get(i2));
                }
                for (int i2 = i1; i2 < entries.size(); i2++) {
                    KeyBindingEntry entry2 = entries.get(i2);
                    if (used.add(entry2.name())) {
                        result = result.add(entry2);
                    } else {
                        for (int i3 = 1; ; i3++) {
                            String newName = entry2.name() + " (" + i3 + ")";
                            if (used.add(newName)) {
                                result = result.add(entry2.withName(newName));
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        }

        return entries;
    }

    private static ImmutableList<KeyBindingEntry> removeMultiAssignments(ImmutableList<KeyBindingEntry> entries) {
        boolean[] used = new boolean[KeyBindingEntry.KEYS_COUNT];
        return entries.replaceAll(e -> {
            if (e.keyIndex() == KeyBindingEntry.UNASSIGNED) {
                return e;
            }
            if (used[e.keyIndex()]) {
                return e.withKeyIndex(KeyBindingEntry.UNASSIGNED);
            }
            used[e.keyIndex()] = true;
            return e;
        });
    }

    // <generated-code>

    public KeyBindingsConfig withEntries(ImmutableList<KeyBindingEntry> entries) {
        return Builder.from(this).entries(entries).build();
    }

    public static class Builder {

        private ImmutableList<KeyBindingEntry> entries;

        private Builder() {}

        public static Builder from(KeyBindingsConfig config) {
            return new Builder()
                    .entries(config.entries);
        }

        public Builder entries(ImmutableList<KeyBindingEntry> entries) {
            this.entries = entries;
            return this;
        }

        public KeyBindingsConfig build() {
            return new KeyBindingsConfig(entries);
        }
    }

    // </generated-code>
}