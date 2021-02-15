package io.github.levtey.CustomCrafting;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Getter;

public class RecipeGUI implements InventoryHolder {
	
	private CustomCrafting plugin;
	private Inventory inventory;
	@Getter
	private boolean shaped = true;
	private boolean[] exactChoices = new boolean[9];
	private NamespacedKey key;
	
	private static final int size = 54;
	private final ItemStack fillerItem;
	private final ItemStack shapedItem;
	private final ItemStack shapelessItem;
	private final ItemStack exactItem;
	private final ItemStack materialItem;
	private final ItemStack confirmItem;
	private static final int[] recipeSlots = new int[] {1, 2, 3, 10, 11, 12, 19, 20, 21};
	public static final int shapedSlot = 29;
	public static final int resultSlot = 38;
	public static final int confirmSlot = 42;
	private static final int[] choiceSlots = new int[] {5, 6, 7, 14, 15, 16, 23, 24, 25};
	
	public RecipeGUI(CustomCrafting plugin, NamespacedKey key) {
		this.plugin = plugin;
		this.key = key;
		fillerItem = plugin.itemFromConfig("inventory.filler");
		shapedItem = plugin.itemFromConfig("inventory.shaped");
		shapelessItem = plugin.itemFromConfig("inventory.shapeless");
		exactItem = plugin.itemFromConfig("inventory.exact");
		materialItem = plugin.itemFromConfig("inventory.material");
		confirmItem = plugin.itemFromConfig("inventory.confirm");
		createInventory();
	}
	
	private void createInventory() {
		inventory = Bukkit.createInventory(this, size, plugin.makeReadable(plugin.getConfig().getString("inventory.name")));
		for (int i = 0; i < size; i++) {
			if (!isFillerSlot(i)) continue;
			inventory.setItem(i, fillerItem);
		}
		
		updateShaped();
		for (int i = 0; i < exactChoices.length; i++) {
			updateChoice(i);
		}
		inventory.setItem(confirmSlot, confirmItem);
	}
	
	@SuppressWarnings("deprecation")
	public Recipe createRecipe() {
		ItemStack result = inventory.getItem(resultSlot);
		if (result == null) return null;
		if (shaped) {
			ShapedRecipe recipe = new ShapedRecipe(key, result);
			recipe.shape("012", "345", "678");
			for (int i = 0; i < 9; i++) {
				ItemStack recipeItem = inventory.getItem(recipeSlots[i]);
				if (recipeItem == null) continue;
				char digitChar = (char) (i + 48);
				recipe.setIngredient(digitChar, exactChoices[i] ? new RecipeChoice.ExactChoice(recipeItem) : new RecipeChoice.MaterialChoice(recipeItem.getType()));
			}
			return recipe;
		} else {
			ShapelessRecipe recipe = new ShapelessRecipe(key, result);
			for (int i = 0; i < 9; i++) {
				ItemStack recipeItem = inventory.getItem(recipeSlots[i]);
				recipe.addIngredient(exactChoices[i] ? new RecipeChoice.ExactChoice(recipeItem) : new RecipeChoice.MaterialChoice(recipeItem.getType()));
			}
			return recipe;
		}
	}
	
	public int choiceIndexOfSlot(int slot) {
		for (int i = 0; i < choiceSlots.length; i++) {
			if (slot == choiceSlots[i]) return i;
		}
		return -1;
	}
	
	public boolean toggleChoice(int slot) {
		slot = Math.min(slot, 8);
		exactChoices[slot] = !exactChoices[slot];
		updateChoice(slot);
		return exactChoices[slot];
	}
	
	public void updateChoice(int slot) {
		inventory.setItem(choiceSlots[slot], exactChoices[slot] ? exactItem : materialItem);
	}
	
	public boolean toggleShaped() {
		shaped = !shaped;
		updateShaped();
		return shaped;
	}
	
	public void updateShaped() {
		inventory.setItem(shapedSlot, shaped ? shapedItem : shapelessItem);
	}
	
	public boolean isFillerSlot(int slot) {
		return !isRecipeSlot(slot) && shapedSlot != slot && resultSlot != slot && confirmSlot != slot && !isChoiceSlot(slot);
	}
	
	public boolean isRecipeSlot(int slot) {
		for (int i = 0; i < recipeSlots.length; i++) {
			if (slot == recipeSlots[i]) return true;
		}
		return false;
	}
	
	public boolean isChoiceSlot(int slot) {
		for (int i = 0; i < choiceSlots.length; i++) {
			if (slot == choiceSlots[i]) return true;
		}
		return false;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}