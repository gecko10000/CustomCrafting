package io.github.levtey.CustomCrafting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

import lombok.Getter;

public class ChoiceGUI implements InventoryHolder {
	
	private static final int size = 54;
	private CustomCrafting plugin;
	private Inventory inventory;
	@Getter
	private RecipeGUI recipeGUI;
	public int recipeSlot;
	private boolean exact = true;
	
	private static final int[] fillerSlots = new int[] {45, 46, 48, 49, 50, 52, 53};
	public static final int exactSlot = 47;
	public static final int confirmSlot = 51;
	private final ItemStack fillerItem;
	private final ItemStack exactItem;
	private final ItemStack materialItem;
	private final ItemStack confirmItem;
	
	@SuppressWarnings("deprecation")
	public ChoiceGUI(CustomCrafting plugin, RecipeGUI recipeGUI, int slot) {
		this.plugin = plugin;
		this.recipeGUI = recipeGUI;
		this.recipeSlot = slot;
		fillerItem = plugin.itemFromConfig("choiceInv.filler");
		exactItem = plugin.itemFromConfig("choiceInv.exact");
		materialItem = plugin.itemFromConfig("choiceInv.material");
		confirmItem = plugin.itemFromConfig("choiceInv.confirm");
		createInventory();
		RecipeChoice choice = recipeGUI.getIngredients()[slot];
		if (choice == null) return;
		if (choice instanceof ExactChoice) {
			List<ItemStack> choices = ((ExactChoice) choice).getChoices();
			for (int i = 0; i < choices.size(); i++) {
				inventory.setItem(i, choices.get(i));
			}
		} else {
			List<Material> choices = ((MaterialChoice) choice).getChoices();
			for (int i = 0; i < choices.size(); i++) {
				inventory.setItem(i, new ItemStack(choices.get(i)));
			}
		}
	}
	
	private void createInventory() {
		inventory = Bukkit.createInventory(this, size, plugin.makeReadable(plugin.getConfig().getString("choiceInv.name")));
		for (int slot : fillerSlots) {
			inventory.setItem(slot, fillerItem);
		}
		inventory.setItem(exactSlot, exact ? exactItem : materialItem);
		inventory.setItem(confirmSlot, confirmItem);
	}
	
	public boolean isClickableSlot(int slot) {
		return !isFillerSlot(slot) && slot != exactSlot && slot != confirmSlot;
	}
	
	public boolean isFillerSlot(int slot) {
		for (int i = 0; i < fillerSlots.length; i++) {
			if (slot == fillerSlots[i]) return true;
		}
		return false;
	}
	
	public void toggleExact() {
		exact = !exact;
		updateExact();
	}
	
	private void updateExact() {
		inventory.setItem(exactSlot, exact ? exactItem : materialItem);
	}
	
	@SuppressWarnings("deprecation")
	public RecipeChoice getChoice() {
		List<ItemStack> items = getItems();
		if (items.isEmpty()) return null;
		if (exact) {
			ExactChoice choice = new ExactChoice(items);
			return choice;
		} else {
			MaterialChoice choice = new MaterialChoice(items.stream().map(ItemStack::getType).collect(Collectors.toList()));
			return choice;
		}
	}
	
	public List<ItemStack> getItems() {
		List<ItemStack> items = new ArrayList<>();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (!isClickableSlot(i) || contents[i] == null || contents[i].getType() == Material.AIR) continue;
			items.add(contents[i]);
		}
		return items;
	}
	
	public ChoiceGUI open(HumanEntity ent) {
		ent.openInventory(inventory);
		return this;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}
