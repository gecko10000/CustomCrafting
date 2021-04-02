package io.github.levtey.CustomCrafting;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.levtey.CustomCrafting.crafting.ChoiceGUI;
import io.github.levtey.CustomCrafting.crafting.CraftingRecipeGUI;

public class Listeners implements Listener {

	private CustomCrafting plugin;
	
	public Listeners(CustomCrafting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onRecipeClick(InventoryClickEvent evt) {
		if ((evt.getInventory().getHolder() instanceof CraftingRecipeGUI) && evt.getClick() == ClickType.DOUBLE_CLICK) {
			evt.setCancelled(true);
			return;
		}
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null) return;
		InventoryHolder holder = clickedInv.getHolder();
		if (!(holder instanceof CraftingRecipeGUI)) return;
		CraftingRecipeGUI recipeGUI = (CraftingRecipeGUI) holder;
		int slot = evt.getSlot();
		if (recipeGUI.isFillerSlot(slot)) {
			evt.setCancelled(true);
			return;
		}
		if (slot == CraftingRecipeGUI.shapedSlot) {
			evt.setCancelled(true);
			recipeGUI.toggleShaped();
			return;
		}
		if (slot == CraftingRecipeGUI.confirmSlot) {
			evt.setCancelled(true);
			if (recipeGUI.getInventory().getItem(CraftingRecipeGUI.resultSlot) == null) {
				plugin.sendMessage(evt.getWhoClicked(), "needResult");
				return;
			}
			recipeGUI.finish();
			CraftingRecipeGUI.ignoreClose = true;
			evt.getWhoClicked().closeInventory();
			CraftingRecipeGUI.ignoreClose = false;
		}
		if (recipeGUI.recipeIndex(slot) != -1) {
			evt.setCancelled(true);
			ItemStack cursor = new ItemStack(evt.getCursor());
			evt.getView().setCursor(null);
			CraftingRecipeGUI.ignoreClose = true;
			new ChoiceGUI(plugin, recipeGUI, recipeGUI.recipeIndex(slot)).open(evt.getWhoClicked()).getInventory().addItem(cursor);
			CraftingRecipeGUI.ignoreClose = false;
			return;
		}
	}
	
	@EventHandler
	public void onChoiceClick(InventoryClickEvent evt) {
		if ((evt.getInventory().getHolder() instanceof ChoiceGUI) && evt.getClick() == ClickType.DOUBLE_CLICK) {
			evt.setCancelled(true);
			return;
		}
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null) return;
		InventoryHolder holder = clickedInv.getHolder();
		if (!(holder instanceof ChoiceGUI)) return;
		ChoiceGUI choiceGUI = (ChoiceGUI) holder;
		int slot = evt.getSlot();
		if (choiceGUI.isFillerSlot(slot)) {
			evt.setCancelled(true);
			return;
		}
		if (slot == ChoiceGUI.exactSlot) {
			evt.setCancelled(true);
			choiceGUI.toggleExact();
			return;
		}
		if (slot == ChoiceGUI.confirmSlot) {
			evt.setCancelled(true);
			if (plugin.getConfig().getBoolean("choiceGUI.returnItems")) {
				evt.getWhoClicked().getInventory().addItem(choiceGUI.getItems().toArray(new ItemStack[0]));
			}
			CraftingRecipeGUI recipeGUI = choiceGUI.getRecipeGUI();
			recipeGUI.setChoice(choiceGUI.recipeSlot, choiceGUI.getChoice());
			recipeGUI.open(evt.getWhoClicked());
			return;
		}
	}
	
	@EventHandler
	public void on(InventoryCloseEvent evt) {
		if (CraftingRecipeGUI.ignoreClose) return;
		InventoryHolder holder = evt.getInventory().getHolder();
		if (!(holder instanceof CraftingRecipeGUI)) return;
		CraftingRecipeGUI recipeGUI = (CraftingRecipeGUI) holder;
		plugin.getSavedEditors().put(recipeGUI.getKey(), recipeGUI);
	}
	
	@EventHandler
	public void on(CraftItemEvent evt) {
		Recipe recipe = evt.getRecipe();
		NamespacedKey key = null;
		if (recipe instanceof ShapedRecipe) {
			key = ((ShapedRecipe) recipe).getKey();
		} else if (recipe instanceof ShapelessRecipe) {
			key = ((ShapelessRecipe) recipe).getKey();
		}
		if (key == null) return;
		if (!key.getNamespace().equalsIgnoreCase(plugin.getName())) {
			return;
		} else {
			if (plugin.getRecipeConfig().getBoolean(key.getKey() + ".restricted")
					&& !evt.getWhoClicked().hasPermission("customcrafting.craft." + key.getKey())) {
				evt.setCancelled(true);
				evt.getWhoClicked().sendMessage(plugin.makeReadable(plugin.getLang().getString("noCraftPerms")));
			}
		}
	}
	
}
