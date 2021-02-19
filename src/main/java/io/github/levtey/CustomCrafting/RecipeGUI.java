package io.github.levtey.CustomCrafting;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
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
	
	public static boolean ignoreClose = true;
	private CustomCrafting plugin;
	private Inventory inventory;
	@Getter
	private boolean shaped = false;
	@Getter
	private NamespacedKey key;
	
	private static final int size = 27;
	private final ItemStack fillerItem;
	private final ItemStack shapedItem;
	private final ItemStack shapelessItem;
	private final ItemStack confirmItem;
	private final ItemStack emptyItem;
	@Getter
	private RecipeChoice[] ingredients = new RecipeChoice[9];
	private static final int[] recipeSlots = new int[] {1, 2, 3, 10, 11, 12, 19, 20, 21};
	public static final int shapedSlot = 13;
	public static final int resultSlot = 14;
	public static final int confirmSlot = 17;
	
	public RecipeGUI(CustomCrafting plugin, NamespacedKey key) {
		this.plugin = plugin;
		this.key = key;
		fillerItem = plugin.itemFromConfig("recipeInv.filler");
		shapedItem = plugin.itemFromConfig("recipeInv.shaped");
		shapelessItem = plugin.itemFromConfig("recipeInv.shapeless");
		confirmItem = plugin.itemFromConfig("recipeInv.confirm");
		emptyItem = plugin.itemFromConfig("recipeInv.empty");
		createInventory();
	}
	
	private void createInventory() {
		inventory = Bukkit.createInventory(this, size, plugin.makeReadable(plugin.getConfig().getString("recipeInv.name")));
		for (int i = 0; i < size; i++) {
			if (!isFillerSlot(i)) continue;
			inventory.setItem(i, fillerItem);
		}
		for (int i = 0; i < ingredients.length; i++) {
			setChoice(i, ingredients[i]);
		}
		updateShaped();
		inventory.setItem(confirmSlot, confirmItem);
	}
	
	@SuppressWarnings("deprecation")
	public void setChoice(int index, RecipeChoice choice) {
		ingredients[index] = choice;
		inventory.setItem(recipeSlots[index], choice == null ? emptyItem : choice.getItemStack());
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
		return recipeIndex(slot) == -1 && shapedSlot != slot && resultSlot != slot && confirmSlot != slot;
	}
	
	public int recipeIndex(int slot) {
		for (int i = 0; i < recipeSlots.length; i++) {
			if (slot == recipeSlots[i]) return i;
		}
		return -1;
	}
	
	public void finish() {
		if (shaped) {
			ShapedRecipe recipe = new ShapedRecipe(key, inventory.getItem(resultSlot));
			recipe.shape("012", "345", "678");
			for (int i = 0; i < 9; i++) {
				recipe.setIngredient((char) (i + '0'), ingredients[i]);
			}
			plugin.saveRecipe(recipe);
			Bukkit.removeRecipe(key);
			Bukkit.addRecipe(recipe);
		} else {
			ShapelessRecipe recipe = new ShapelessRecipe(key, inventory.getItem(resultSlot));
			for (RecipeChoice choice : ingredients) {
				if (choice == null) continue;
				recipe.addIngredient(choice);
			}
			plugin.saveRecipe(recipe);
			Bukkit.removeRecipe(key);
			Bukkit.addRecipe(recipe);
		}
	}
	
	public void open(HumanEntity ent) {
		ent.openInventory(inventory);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}