package io.github.levtey.CustomCrafting;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

public class CustomCraftingCommand implements CommandExecutor {
	
	private CustomCrafting plugin;
	
	public CustomCraftingCommand(CustomCrafting plugin) {
		this.plugin = plugin;
		plugin.getCommand("customcrafting").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("customcrafting.admin")) return plugin.sendMessage(sender, "noPerms");
		if (!(sender instanceof Player)) return plugin.sendMessage(sender, "notPlayer");
		if (args.length <= 1) return plugin.sendMessage(sender, "usage");
		Recipe recipe = Bukkit.getRecipe(new NamespacedKey(plugin, args[1]));
		switch (args[0].toLowerCase()) {
		case "create":
			if (recipe != null) return plugin.sendMessage(sender, "exists");
			((Player) sender).openInventory(new RecipeGUI(plugin, new NamespacedKey(plugin, args[1])).getInventory());
			break;
		//case "edit":
			//if (recipe == null || (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe))) return plugin.sendMessage(sender, "invalid");
			//new RecipeGUI(plugin, new NamespacedKey(plugin, args[1]));
		}
		return true;
	}
	
}
