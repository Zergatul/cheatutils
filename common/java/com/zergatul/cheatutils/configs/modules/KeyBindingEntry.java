package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import org.jspecify.annotations.Nullable;

public record KeyBindingEntry(
        String name,
        @Nullable String code,
        int keyIndex
) implements ImmutableConfig {

    public static final int KEYS_COUNT = 30;
    public static final int UNASSIGNED = -1;

    public KeyBindingEntry {
        if (name == null) {
            name = "";
        }

        if (code != null && code.isEmpty()) {
            code = null;
        }

        if (!isValidKeyIndex(keyIndex)) {
            keyIndex = UNASSIGNED;
        }
    }

    public static KeyBindingEntry defaults() {
        return new Builder()
                .name("Missing")
                .code(null)
                .keyIndex(UNASSIGNED)
                .build();
    }

    public static boolean isValidKeyIndex(int keyIndex) {
        return (keyIndex >= 0 && keyIndex < KEYS_COUNT) || keyIndex == UNASSIGNED;
    }

    // <generated-code>

    public KeyBindingEntry withName(String name) {
        return Builder.from(this).name(name).build();
    }

    public KeyBindingEntry withCode(@Nullable String code) {
        return Builder.from(this).code(code).build();
    }

    public KeyBindingEntry withKeyIndex(int keyIndex) {
        return Builder.from(this).keyIndex(keyIndex).build();
    }

    public static class Builder {

        private String name;
        private @Nullable String code;
        private int keyIndex;

        private Builder() {}

        public static Builder from(KeyBindingEntry config) {
            return new Builder()
                    .name(config.name)
                    .code(config.code)
                    .keyIndex(config.keyIndex);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder code(@Nullable String code) {
            this.code = code;
            return this;
        }

        public Builder keyIndex(int keyIndex) {
            this.keyIndex = keyIndex;
            return this;
        }

        public KeyBindingEntry build() {
            return new KeyBindingEntry(name, code, keyIndex);
        }
    }

    // </generated-code>
}