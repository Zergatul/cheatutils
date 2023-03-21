package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.AutoCraftConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.CraftingScreenMixinInterface;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AutoCraftController {

    public static final AutoCraftController instance = new AutoCraftController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoCraftController.class);
    private State state = State.NONE;

    private AutoCraftController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null || mc.level == null) {
            state = State.NONE;
            return;
        }

        AutoCraftConfig config = ConfigStore.instance.getConfig().autoCraftConfig;
        if (!config.enabled) {
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

        try {
            switch (state) {
                case START:
                    ImmutableList<ItemStack> inventory = new ImmutableList<>(mc.player.getInventory().items.stream().map(ItemStack::copy).toList());
                    for (Item item: config.items) {
                        CraftEntry entry = getCraftingEntry(item, inventory, List.of(item));
                        if (entry != null) {
                            boolean shift = entry.recipe.getResultItem(mc.level.registryAccess()).getItem().getMaxStackSize(entry.recipe.getResultItem(mc.level.registryAccess())) > 1;
                            mc.gameMode.handlePlaceRecipe(craftingScreen.getMenu().containerId, entry.recipe, shift);
                            state = State.RECIPE_CLICKED;
                        }
                    }
                    break;

                case RECIPE_CLICKED:
                    Slot slot = craftingScreen.getMenu().slots.get(CraftingMenu.RESULT_SLOT);
                    if (slot.hasItem()) {
                        ((CraftingScreenMixinInterface) craftingScreen).triggerSlotClicked(slot, 0, 0, ClickType.QUICK_MOVE);
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
    }

    private CraftEntry getCraftingEntry(Item item, ImmutableList<ItemStack> inventory, List<Item> exclude) {
        List<Recipe<?>> recipes = mc.player.getRecipeBook().getCollections().stream()
                .flatMap(c -> c.getRecipes().stream())
                .filter(r -> r.getResultItem(mc.level.registryAccess()).getItem() == item)
                .filter(r -> r.getIngredients().stream().noneMatch(ingr -> exclude.stream().anyMatch(i -> Arrays.stream(ingr.getItems()).anyMatch(is -> is.is(i)))))
                .toList();

        CraftingPlanEntry[] planEntries = new CraftingPlanEntry[recipes.size()];
        for (int i = 0; i < recipes.size(); i++) {
            planEntries[i] = new CraftingPlanEntry(recipes.get(i), getMissingIngredients(recipes.get(i), inventory, exclude));
        }

        Arrays.sort(planEntries, Comparator.comparingInt(pe -> pe.missing.size()));
        for (CraftingPlanEntry planEntry: planEntries) {
            if (planEntry.missing.size() == 0) {
                return new CraftEntry(planEntry, inventory);
            }

            for (Ingredient missingIngredient: planEntry.missing) {
                for (ItemStack possibleItemStack: missingIngredient.getItems()) {
                    Item tryItem = possibleItemStack.getItem();
                    List<Item> newExclude = new ArrayList<>(exclude);
                    newExclude.add(tryItem);
                    CraftEntry subPlan = getCraftingEntry(tryItem, inventory, newExclude);
                    if (subPlan != null) {
                        return subPlan;
                    }
                }
            }
        }

        return null;
    }

    private List<Ingredient> getMissingIngredients(Recipe<?> recipe, ImmutableList<ItemStack> inventory, List<Item> exclude) {
        List<Ingredient> list = new ArrayList<>();
        for (Ingredient ingredient: recipe.getIngredients()) {
            boolean has = false;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack itemStack = inventory.get(i);
                if (testIngredient(ingredient, itemStack, exclude)) {
                    ItemStack newItemStack = itemStack.copy();
                    newItemStack.setCount(itemStack.getCount() - 1);
                    inventory = inventory.set(i, newItemStack);
                    has = true;
                    break;
                }
            }
            if (!has) {
                list.add(ingredient);
            }
        }
        return list;
    }

    private boolean testIngredient(Ingredient ingredient, ItemStack itemStack, List<Item> exclude) {
        if (!ingredient.test(itemStack)) {
            return false;
        }

        if (itemStack == null) {
            return false;
        } else {
            if (ingredient.getItems().length == 0) {
                return itemStack.isEmpty();
            } else {
                for (ItemStack ingrItemStack: ingredient.getItems()) {
                    if (exclude.stream().anyMatch(ingrItemStack::is)) {
                        continue;
                    }
                    if (ingrItemStack.is(itemStack.getItem())) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private enum State {
        NONE,
        START,
        RECIPE_CLICKED,
        RESULT_CLICKED,
        INVALID
    }

    private static class CraftingPlanEntry {
        public Recipe<?> recipe;
        public List<Ingredient> missing;

        public CraftingPlanEntry(Recipe<?> recipe, List<Ingredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }
    }

    private static class CraftEntry {

        public Recipe<?> recipe;
        public ItemStack[] itemStacks = new ItemStack[9];
        public ImmutableList<ItemStack> inventory;

        public CraftEntry(CraftingPlanEntry entry, ImmutableList<ItemStack> inventory) {
            this.recipe = entry.recipe;
            this.inventory = inventory;

            if (entry.recipe instanceof ShapedRecipe shapedRecipe) {
                for (int i = 0; i < 9; i++) {
                    int x = i % 3;
                    int y = i / 3;
                    if (x < shapedRecipe.getWidth() && y < shapedRecipe.getHeight()) {
                        int index = y * shapedRecipe.getWidth() + x;
                        itemStacks[i] = applyIngredient(entry.recipe.getIngredients().get(index));
                    } else {
                        itemStacks[i] = ItemStack.EMPTY;
                    }
                }
            } else {
                for (int i = 0; i < entry.recipe.getIngredients().size(); i++) {
                    itemStacks[i] = applyIngredient(entry.recipe.getIngredients().get(i));
                }
                for (int i = entry.recipe.getIngredients().size(); i < 9; i++) {
                    itemStacks[i] = ItemStack.EMPTY;
                }
            }
        }

        private ItemStack applyIngredient(Ingredient ingredient) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack itemStack = inventory.get(i);
                if (ingredient.test(itemStack)) {
                    ItemStack newItemStack = itemStack.copy();
                    newItemStack.setCount(itemStack.getCount() - 1);
                    inventory = inventory.set(i, newItemStack);

                    ItemStack craftItemStack = itemStack.copy();
                    craftItemStack.setCount(1);
                    return craftItemStack;
                }
            }

            throw new IllegalStateException("Cannot apply Ingredient in CraftEntry at current Inventory state.");
        }
    }
}