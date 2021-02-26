package io.github.levtey.CustomCrafting;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import me.arcaniax.hdb.api.DatabaseLoadEvent;

public class Listeners implements Listener {

	private CustomCrafting plugin;
	
	public Listeners(CustomCrafting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onRecipeClick(InventoryClickEvent evt) {
		if ((evt.getInventory().getHolder() instanceof RecipeGUI) && evt.getClick() == ClickType.DOUBLE_CLICK) {
			evt.setCancelled(true);
			return;
		}
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null) return;
		InventoryHolder holder = clickedInv.getHolder();
		if (!(holder instanceof RecipeGUI)) return;
		RecipeGUI recipeGUI = (RecipeGUI) holder;
		int slot = evt.getSlot();
		if (recipeGUI.isFillerSlot(slot)) {
			evt.setCancelled(true);
			return;
		}
		if (slot == RecipeGUI.shapedSlot) {
			evt.setCancelled(true);
			recipeGUI.toggleShaped();
			return;
		}
		if (slot == RecipeGUI.confirmSlot) {
			evt.setCancelled(true);
			if (recipeGUI.getInventory().getItem(RecipeGUI.resultSlot) == null) {
				plugin.sendMessage(evt.getWhoClicked(), "needResult");
				return;
			}
			recipeGUI.finish();
			RecipeGUI.ignoreClose = true;
			evt.getWhoClicked().closeInventory();
			RecipeGUI.ignoreClose = false;
		}
		if (recipeGUI.recipeIndex(slot) != -1) {
			evt.setCancelled(true);
			ItemStack cursor = new ItemStack(evt.getCursor());
			evt.getView().setCursor(null);
			RecipeGUI.ignoreClose = true;
			new ChoiceGUI(plugin, recipeGUI, recipeGUI.recipeIndex(slot)).open(evt.getWhoClicked()).getInventory().addItem(cursor);
			RecipeGUI.ignoreClose = false;
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
			RecipeGUI recipeGUI = choiceGUI.getRecipeGUI();
			recipeGUI.setChoice(choiceGUI.recipeSlot, choiceGUI.getChoice());
			recipeGUI.open(evt.getWhoClicked());
			return;
		}
	}
	
	@EventHandler
	public void on(InventoryCloseEvent evt) {
		if (RecipeGUI.ignoreClose) return;
		InventoryHolder holder = evt.getInventory().getHolder();
		if (!(holder instanceof RecipeGUI)) return;
		RecipeGUI recipeGUI = (RecipeGUI) holder;
		plugin.getSavedEditors().put(recipeGUI.getKey(), recipeGUI);
	}
	
}
