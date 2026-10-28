package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import net.minecraft.world.item.Item;

public record ContainerButtonsConfig(
        boolean showTakeAll,
        boolean showSmartPut,
        boolean checkShulkerContent,
        boolean showDropAll,
        boolean autoTakeAll,
        boolean autoDropAll,
        boolean autoClose,
        boolean useFilter,
        ImmutableList<Item> items
) implements ImmutableConfig, ModuleStateProvider {

    public ContainerButtonsConfig {
        if (autoTakeAll && autoDropAll) {
            // these 2 options can't be used at the same time
            autoTakeAll = false;
            autoDropAll = false;
        }
        if (items == null) {
            items = ImmutableList.empty();
        }
    }

    public static ContainerButtonsConfig defaults() {
        return new Builder()
                .showTakeAll(false)
                .showSmartPut(false)
                .checkShulkerContent(false)
                .showDropAll(false)
                .autoTakeAll(false)
                .autoDropAll(false)
                .autoClose(false)
                .useFilter(false)
                .items(ImmutableList.empty())
                .build();
    }

    @Override
    public boolean isEnabled() {
        return showTakeAll || showSmartPut || showDropAll || autoTakeAll || autoDropAll || autoClose;
    }

    // <generated-code>

    public ContainerButtonsConfig withShowTakeAll(boolean showTakeAll) {
        return Builder.from(this).showTakeAll(showTakeAll).build();
    }

    public ContainerButtonsConfig withShowSmartPut(boolean showSmartPut) {
        return Builder.from(this).showSmartPut(showSmartPut).build();
    }

    public ContainerButtonsConfig withCheckShulkerContent(boolean checkShulkerContent) {
        return Builder.from(this).checkShulkerContent(checkShulkerContent).build();
    }

    public ContainerButtonsConfig withShowDropAll(boolean showDropAll) {
        return Builder.from(this).showDropAll(showDropAll).build();
    }

    public ContainerButtonsConfig withAutoTakeAll(boolean autoTakeAll) {
        return Builder.from(this).autoTakeAll(autoTakeAll).build();
    }

    public ContainerButtonsConfig withAutoDropAll(boolean autoDropAll) {
        return Builder.from(this).autoDropAll(autoDropAll).build();
    }

    public ContainerButtonsConfig withAutoClose(boolean autoClose) {
        return Builder.from(this).autoClose(autoClose).build();
    }

    public ContainerButtonsConfig withUseFilter(boolean useFilter) {
        return Builder.from(this).useFilter(useFilter).build();
    }

    public ContainerButtonsConfig withItems(ImmutableList<Item> items) {
        return Builder.from(this).items(items).build();
    }

    public static class Builder {

        private boolean showTakeAll;
        private boolean showSmartPut;
        private boolean checkShulkerContent;
        private boolean showDropAll;
        private boolean autoTakeAll;
        private boolean autoDropAll;
        private boolean autoClose;
        private boolean useFilter;
        private ImmutableList<Item> items;

        private Builder() {}

        public static Builder from(ContainerButtonsConfig config) {
            return new Builder()
                    .showTakeAll(config.showTakeAll)
                    .showSmartPut(config.showSmartPut)
                    .checkShulkerContent(config.checkShulkerContent)
                    .showDropAll(config.showDropAll)
                    .autoTakeAll(config.autoTakeAll)
                    .autoDropAll(config.autoDropAll)
                    .autoClose(config.autoClose)
                    .useFilter(config.useFilter)
                    .items(config.items);
        }

        public Builder showTakeAll(boolean showTakeAll) {
            this.showTakeAll = showTakeAll;
            return this;
        }

        public Builder showSmartPut(boolean showSmartPut) {
            this.showSmartPut = showSmartPut;
            return this;
        }

        public Builder checkShulkerContent(boolean checkShulkerContent) {
            this.checkShulkerContent = checkShulkerContent;
            return this;
        }

        public Builder showDropAll(boolean showDropAll) {
            this.showDropAll = showDropAll;
            return this;
        }

        public Builder autoTakeAll(boolean autoTakeAll) {
            this.autoTakeAll = autoTakeAll;
            return this;
        }

        public Builder autoDropAll(boolean autoDropAll) {
            this.autoDropAll = autoDropAll;
            return this;
        }

        public Builder autoClose(boolean autoClose) {
            this.autoClose = autoClose;
            return this;
        }

        public Builder useFilter(boolean useFilter) {
            this.useFilter = useFilter;
            return this;
        }

        public Builder items(ImmutableList<Item> items) {
            this.items = items;
            return this;
        }

        public ContainerButtonsConfig build() {
            return new ContainerButtonsConfig(showTakeAll, showSmartPut, checkShulkerContent, showDropAll, autoTakeAll, autoDropAll, autoClose, useFilter, items);
        }
    }

    // </generated-code>
}