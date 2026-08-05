package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.common.events.ContainerScreenRenderEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerSummaryConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContainerSummary implements Module {

    public static final ContainerSummary instance = new ContainerSummary();

    private static final int BORDER_WIDTH = 2;
    private final Minecraft mc = Minecraft.getInstance();
    private final Identifier background = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    private ContainerSummary() {
        Events.ContainerScreenAfterRenderContents.add(this::onAfterRenderContents);
    }

    private void onAfterRenderContents(ContainerScreenRenderEvent event) {
        ContainerSummaryConfig config = ConfigStore.instance.getConfig().containerSummaryConfig;
        if (!config.enabled) {
            return;
        }

        NonNullList<Slot> slots = event.screen().getMenu().slots;
        if (slots.isEmpty()) {
            return;
        }

        List<ItemStack> items = new ArrayList<>();
        if (slots.getFirst().container instanceof SimpleContainer container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack itemStack = container.getItem(i);
                if (!itemStack.isEmpty()) {
                    items.add(itemStack);
                }
            }
        } else {
            if (config.showForInventory) {
                if (event.screen() instanceof InventoryScreen) {
                    for (Slot slot : slots) {
                        if (slot.container instanceof ResultContainer) {
                            continue;
                        }
                        if (!slot.getItem().isEmpty()) {
                            items.add(slot.getItem());
                        }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            return;
        }

        List<ItemDrawable> list = groupItems(items);
        for (ItemDrawable item : list) {
            item.measure(event.screen().getFont(), !mc.hasAltDown());
        }

        List<ItemsColumn> columns = ItemsColumn.split(list);
        for (ItemsColumn column : columns) {
            column.measure();
        }

        int baseX = event.leftPos() + event.imageWidth() + 2;
        int baseY = event.topPos();

        drawBorders(event.graphics(), columns, baseX, baseY);

        int cursorX = baseX + BORDER_WIDTH;
        for (ItemsColumn column : columns) {
            int cursorY = baseY + BORDER_WIDTH;
            for (ItemDrawable item : column.list) {
                item.draw(event.graphics(), background, event.screen().getFont(), cursorX, cursorY, column.getWidth());
                cursorY += item.getHeight();
            }
            cursorX += column.getWidth();
        }
    }

    private List<ItemDrawable> groupItems(List<ItemStack> items) {
        Map<Item, ItemDrawable> map = new HashMap<>();
        for (ItemStack itemStack: items) {
            if (itemStack.is(Items.AIR)) {
                continue;
            }

            addItem(map, itemStack);

            if (ItemUtils.isShulkerBox(itemStack)) {
                for (ItemStack slot : ItemUtils.getShulkerContent(itemStack)) {
                    if (!slot.isEmpty()) {
                        addItem(map, slot);
                    }
                }
            }

            if (itemStack.getItem() instanceof BundleItem) {
                BundleContents contents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
                assert contents != null;
                for (ItemStackTemplate slot : contents.items()) {
                    if (slot.count() != 0) {
                        addItem(map, slot.create());
                    }
                }
            }
        }

        return map.values().stream().sorted((i1, i2) -> {
            int compare = -Integer.compare(i1.count, i2.count);
            if (compare != 0) {
                return compare;
            }
            String id1 = Registries.ITEMS.getKey(i1.item).toString();
            String id2 = Registries.ITEMS.getKey(i2.item).toString();
            return id1.compareTo(id2);
        }).collect(Collectors.toList());
    }

    private void addItem(Map<Item, ItemDrawable> map, ItemStack itemStack) {
        Item item = itemStack.getItem();
        int count = itemStack.getCount();
        ItemDrawable drawable = map.get(item);
        if (drawable == null) {
            drawable = new ItemDrawable(item);
        }

        drawable.count += count;
        map.put(item, drawable);
    }

    private void drawBorders(GuiGraphicsExtractor graphics, List<ItemsColumn> columns, int baseX, int baseY) {
        int width = columns.stream().map(ItemsColumn::getWidth).reduce(0, Integer::sum);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED, background,
                baseX, baseY,
                5, 15,
                BORDER_WIDTH, columns.getFirst().getHeight() + 2 * BORDER_WIDTH,
                2, 2,
                256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, background,
                baseX + width + BORDER_WIDTH, baseY,
                5, 15,
                BORDER_WIDTH, columns.getFirst().getHeight() + 2 * BORDER_WIDTH,
                2, 2,
                256, 256);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED, background,
                baseX, baseY,
                5, 15,
                width + 2 * BORDER_WIDTH, BORDER_WIDTH,
                2, 2,
                256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, background,
                baseX, baseY + columns.getFirst().getHeight() + BORDER_WIDTH,
                5, 15,
                width + 2 * BORDER_WIDTH, BORDER_WIDTH,
                2, 2,
                256, 256);
    }

    private static class ItemDrawable {

        private static final int TEXT_SHIFT_X = 2;
        private static final int TEXT_SHIFT_Y = 8;

        public Item item;
        public int count;
        public int width;
        private int stackSize;
        private int stacksCount;
        private int remCount;

        public ItemDrawable(Item item) {
            this.item = item;
            this.count = 0;
        }

        public int getHeight() {
            return 18;
        }

        public void measure(Font font, boolean group) {
            stackSize = group ? ItemUtils.getStackSize(item) : 1;
            stacksCount = count / stackSize;
            remCount = count % stackSize;

            width = 0;

            if (stacksCount > 0) {
                if (stacksCount > 1) {
                    width += font.width(stacksCount + "x");
                }
                width += 18;
            }

            if (remCount > 0) {
                if (stacksCount > 0) {
                    width += font.width("+");
                }
                width += 18;
            }
        }

        public void draw(GuiGraphicsExtractor graphics, Identifier background, Font font, int x, int y, int width) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, background,
                    x, y,
                    7, 17,
                    4, 18,
                    4, 18,
                    256, 256);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, background,
                    x + 4, y,
                    11, 17,
                    width - 8, 18,
                    10, 18,
                    256, 256);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, background,
                    x + width - 4, y,
                    21, 17,
                    4, 18,
                    4, 18,
                    256, 256);

            x += width - this.width; // right align

            if (stacksCount > 0) {
                if (stacksCount > 1) {
                    graphics.text(font, stacksCount + "x", x + TEXT_SHIFT_X, y + TEXT_SHIFT_Y, -1, true);
                    x += font.width(stacksCount + "x");
                }

                ItemStack itemStack = new ItemStack(item, stackSize);
                graphics.fakeItem(itemStack, x + 1, y + 1);
                graphics.itemDecorations(font, itemStack, x + 1, y + 1);
                x += 18;

                if (remCount > 0) {
                    graphics.text(font, "+", x + TEXT_SHIFT_X, y + TEXT_SHIFT_Y, -1, true);
                    x += font.width("+");
                }
            }

            if (remCount > 0) {
                ItemStack itemStack = new ItemStack(item, remCount);
                graphics.fakeItem(itemStack, x + 1, y + 1);
                graphics.itemDecorations(font, itemStack, x + 1, y + 1);
            }
        }
    }

    private static class ItemsColumn {

        private static final int MAX_ROWS = 10;

        public List<ItemDrawable> list;
        private int width;
        private int height;

        public ItemsColumn() {
            this.list = new ArrayList<>();
        }

        public static List<ItemsColumn> split(List<ItemDrawable> items) {
            List<ItemsColumn> columns = new ArrayList<>();
            for (ItemDrawable item : items) {
                if (columns.isEmpty() || columns.getLast().list.size() == MAX_ROWS) {
                    columns.add(new ItemsColumn());
                }
                columns.getLast().list.add(item);
            }
            return columns;
        }

        public void measure() {
            width = 0;
            height = 0;
            for (ItemDrawable drawable : list) {
                if (drawable.width > width) {
                    width = drawable.width;
                }
                height += drawable.getHeight();
            }
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}