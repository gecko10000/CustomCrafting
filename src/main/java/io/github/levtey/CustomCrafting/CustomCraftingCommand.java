package io.github.levtey.CustomCrafting;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class CustomCraftingCommand implements CommandExecutor {
	
	private CustomCrafting plugin;
	
	public CustomCraftingCommand(CustomCrafting plugin) {
		this.plugin = plugin;
		plugin.getCommand("customcrafting").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("customcrafting.admin")) return plugin.sendMessage(sender, "noPerms");
		if (!(sender instanceof Player)) return plugin.sendMessage(sender, "notPlayer");
		if (args.length == 0) return plugin.sendMessage(sender, "usage");
		if (args[0].equalsIgnoreCase("reload")) {
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			plugin.reloadLang();
			plugin.reloadRecipeConfig();
			plugin.reloadRecipes();
			return plugin.sendMessage(sender, "reload");
		}
		Recipe recipe = Bukkit.getRecipe(new NamespacedKey(plugin, args[1]));
		switch (args[0].toLowerCase()) {
		case "create":
			if (recipe != null) return plugin.sendMessage(sender, "exists");
		case "edit":
			NamespacedKey recipeKey = new NamespacedKey(plugin, args[1]);
			if (plugin.getSavedEditors().containsKey(recipeKey)) {
				plugin.getSavedEditors().remove(recipeKey).open((Player) sender);
				return true;
			}
			new RecipeGUI(plugin, new NamespacedKey(plugin, args[1])).open((Player) sender);
			break;
		//case "edit":
			//if (recipe == null || (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe))) return plugin.sendMessage(sender, "invalid");
			//new RecipeGUI(plugin, new NamespacedKey(plugin, args[1]));
		}
		return true;
	}
	
}
