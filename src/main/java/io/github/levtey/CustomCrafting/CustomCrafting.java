package io.github.levtey.CustomCrafting;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.Zrips.CMI.Containers.CMIChatColor;

import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

public class CustomCrafting extends JavaPlugin {
	
	private File recipeFile;
	private File langFile;
	@Getter
	private FileConfiguration recipeConfig, lang;
	@Getter
	private HeadDatabaseAPI hdb;

	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadRecipeConfig();
		this.reloadLang();
		new Listeners(this);
		new CustomCraftingCommand(this);
		setHdb();
	}
	
	public void setHdb() {
		hdb = new HeadDatabaseAPI();
	}
	
	public void reloadRecipeConfig() {
		recipeFile = new File(getDataFolder(), "recipes.yml");
		if (!recipeFile.exists()) {
			recipeFile.getParentFile().mkdirs();
			saveResource("recipes.yml", false);
		}
		recipeConfig = new YamlConfiguration();
		try {
			recipeConfig.load(recipeFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void reloadLang() {
		langFile = new File(getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			langFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public ItemStack itemFromConfig(String path) {
		String material = getConfig().getString(path + ".material");
		ItemStack item = null;
		if (material.startsWith("hdb")) {
			item = hdb.getItemHead(material.substring(material.lastIndexOf(':') + 1));
		} else {
			item = new ItemStack(Material.getMaterial(material.toUpperCase()));
		}
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(makeReadable(getConfig().getString(path + ".name")));
		meta.setLore(getConfig().getStringList(path + ".lore").stream().map(this::makeReadable).collect(Collectors.toList()));
		if (getConfig().getBoolean(path + ".enchanted")) {
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean sendMessage(CommandSender sender, String path) {
		sender.sendMessage(makeReadable(getLang().getString(path)));
		return true;
	}
	
	public String makeReadable(String input) {
		return input == null ? null : CMIChatColor.colorize(input);
	}
	
}
