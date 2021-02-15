package io.github.levtey.CustomCrafting;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.arcaniax.hdb.api.DatabaseLoadEvent;

public class Listeners implements Listener {

	private CustomCrafting plugin;
	
	public Listeners(CustomCrafting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void on(InventoryClickEvent evt) {
		if ((evt.getInventory().getHolder() instanceof RecipeGUI) && evt.getClick() == ClickType.DOUBLE_CLICK) {
			evt.setCancelled(true);
			return;
		}
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null) return;
		InventoryHolder holder = clickedInv.getHolder();
		if (holder == null || !(holder instanceof RecipeGUI)) return;
		RecipeGUI gui = (RecipeGUI) holder;
		int slot = evt.getSlot();
		if (gui.isFillerSlot(slot)) {
			evt.setCancelled(true);
			return;
		}
		if (slot == RecipeGUI.shapedSlot) {
			evt.setCancelled(true);
			gui.toggleShaped();
			return;
		}
		if (gui.isChoiceSlot(slot)) {
			evt.setCancelled(true);
			gui.toggleChoice(gui.choiceIndexOfSlot(slot));
			return;
		}
		if (slot == RecipeGUI.confirmSlot) {
			evt.setCancelled(true);
			Bukkit.addRecipe(gui.createRecipe());
			Bukkit.broadcastMessage("order up");
			evt.getView().close();
		}
	}
	
}
