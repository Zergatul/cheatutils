package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoCraftConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.CraftingScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AutoCraft {

    public static final AutoCraft instance = new AutoCraft();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoCraft.class);
    private State state = State.NONE;

    private AutoCraft() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            state = State.NONE;
            return;
        }

        AutoCraftConfig config = ConfigStore.instance.getConfig().autoCraftConfig;
        if (!config.enabled || config.items.length == 0) {
            state = State.NONE;
            return;
        }

        if (!(mc.screen instanceof CraftingScreen craftingScreen)) {
            state = State.NONE;
            return;
        }

        if (state == State.NONE) {
            state = State.START;
        }

        mc.getProfiler().push("auto-craft");

        try {
            switch (state) {
                case START:
                    Recipe<?> recipe = findRecipe(config);
                    if (recipe != null) {
                        boolean shift = recipe.getResultItem().getItem().getMaxStackSize() > 1;
                        mc.gameMode.handlePlaceRecipe(craftingScreen.getMenu().containerId, recipe, shift);
                        state = State.RECIPE_CLICKED;
                    }
                    break;

                case RECIPE_CLICKED:
                    Slot slot = craftingScreen.getMenu().slots.get(CraftingMenu.RESULT_SLOT);
                    if (slot.hasItem()) {
                        ((CraftingScreenAccessor) craftingScreen).slotClicked_CU(slot, 0, 0, ClickType.QUICK_MOVE);
                        state = State.RESULT_CLICKED;
                    }
                    break;

                case RESULT_CLICKED:
                    slot = craftingScreen.getMenu().slots.get(CraftingMenu.RESULT_SLOT);
                    if (!slot.hasItem()) {
                        state = State.START;
                    }
                    break;
            }
        }
        catch (Exception e) {
            logger.error(e);
            config.enabled = false;
        }

        mc.getProfiler().pop();
    }

    private Recipe<?> findRecipe(AutoCraftConfig config) {
        ImmutableList<ItemStack> inventory = new ImmutableList<>(mc.player.getInventory().items.stream().map(ItemStack::copy).toList());
        List<Recipe<?>> recipes = mc.player.getRecipeBook().getCollections().stream()
                .flatMap(c -> c.getRecipes().stream())
                .filter(r -> r instanceof CraftingRecipe)
                .toList();

        for (Item baseItem : config.items) {
            // queue for next recipes to process
            Queue<CraftingTreeEntry> queue = new LinkedList<>();

            // add base recipes to queue
            for (Recipe<?> recipe : recipes) {
                if (!recipe.getResultItem().is(baseItem)) {
                    continue;
                }

                queue.add(new CraftingTreeEntry(recipe, null));
            }

            while (queue.size() > 0) {
                CraftingTreeEntry entry = queue.poll();
                List<Item> missing = getMissingIngredients(entry, inventory);
                if (missing.size() == 0) {
                    // found recipe we can craft
                    return entry.recipe;
                }

                // add missing items to queue
                for (Item item : missing) {
                    // find recipes
                    recipesLoop:
                    for (Recipe<?> recipe : recipes) {
                        if (!recipe.getResultItem().is(item)) {
                            continue;
                        }

                        // don't use the same recipe to prevent loops
                        if (entry.has(recipe)) {
                            continue;
                        }

                        // don't use the same items to prevent loops
                        for (Ingredient ingredient : recipe.getIngredients()) {
                            for (ItemStack itemStack : ingredient.getItems()) {
                                if (entry.has(itemStack.getItem())) {
                                    continue recipesLoop;
                                }
                            }
                        }

                        queue.add(new CraftingTreeEntry(recipe, entry));
                    }
                }
            }
        }

        return null;
    }

    private List<Item> getMissingIngredients(CraftingTreeEntry entry, ImmutableList<ItemStack> inventory) {
        List<Item> list = new ArrayList<>();
        for (Ingredient ingredient : entry.recipe.getIngredients()) {
            boolean has = false;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack itemStack = inventory.get(i);
                if (ingredient.test(itemStack)) {
                    ItemStack newItemStack = itemStack.copy();
                    newItemStack.setCount(itemStack.getCount() - 1);
                    inventory = inventory.set(i, newItemStack);
                    has = true;
                    break;
                }
            }
            if (!has) {
                for (ItemStack itemStack : ingredient.getItems()) {
                    Item item = itemStack.getItem();
                    if (!list.contains(item)) {
                        list.add(item);
                    }
                }
            }
        }
        return list;
    }

    private enum State {
        NONE,
        START,
        RECIPE_CLICKED,
        RESULT_CLICKED,
        INVALID
    }

    private record CraftingTreeEntry(Recipe<?> recipe, AutoCraft.CraftingTreeEntry parent) {

        public boolean has(Recipe<?> recipe) {
            for (CraftingTreeEntry current = this; current != null; current = current.parent) {
                if (current.recipe == recipe) {
                    return true;
                }
            }

            return false;
        }

        public boolean has(Item item) {
            for (CraftingTreeEntry current = this; current != null; current = current.parent) {
                if (current.recipe.getResultItem().is(item)) {
                    return true;
                }
            }

            return false;
        }
    }
}