package io.github.levtey.CustomCrafting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.Zrips.CMI.Containers.CMIChatColor;

import io.github.levtey.CustomCrafting.crafting.CraftingRecipeGUI;
import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

public class CustomCrafting extends JavaPlugin {
	
	private File recipeFile;
	private File langFile;
	@Getter
	private FileConfiguration recipeConfig, lang;
	@Getter
	private HeadDatabaseAPI hdb;
	@Getter
	private Map<NamespacedKey, CraftingRecipeGUI> savedEditors = new HashMap<>();

	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadRecipeConfig();
		this.reloadLang();
		new Listeners(this);
		new CustomCraftingCommand(this);
		reloadRecipes();
		setHdb();
	}
	
	public void onDisable() {
		removeCustomRecipes();
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
	
	public void reloadRecipes() {
		removeCustomRecipes();
		for (String key : recipeConfig.getKeys(false)) {
			Bukkit.addRecipe(loadRecipe(key));
		}
	}
	
	public void removeCustomRecipes() {
		Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
		while (recipeIterator.hasNext()) {
			Recipe recipe = recipeIterator.next();
			if (recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().getNamespace().equals(this.getName().toLowerCase())) {
				Bukkit.removeRecipe(((ShapedRecipe) recipe).getKey());
			} else if (recipe instanceof ShapelessRecipe && ((ShapelessRecipe) recipe).getKey().getNamespace().equals(this.getName().toLowerCase())) {
				Bukkit.removeRecipe(((ShapelessRecipe) recipe).getKey());
			} else if (recipe instanceof FurnaceRecipe && ((FurnaceRecipe) recipe).getKey().getNamespace().equals(this.getName().toLowerCase())) {
				Bukkit.removeRecipe(((FurnaceRecipe) recipe).getKey());
			}
		}
	}
	
	public void saveRecipe(Recipe r) {
		String key = null;
		if (r instanceof ShapedRecipe) {
			ShapedRecipe recipe = (ShapedRecipe) r;
			key = recipe.getKey().getKey();
			recipeConfig.set(key + ".shaped", true);
			Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
			for (int i = 0; i < 9; i++) {
				RecipeChoice choice = choiceMap.get((char) (i + '0'));
				saveChoice(key + ".choices." + i, choice);
			}
		} else if (r instanceof ShapelessRecipe) {
			ShapelessRecipe recipe = (ShapelessRecipe) r;
			key = recipe.getKey().getKey();
			recipeConfig.set(key + ".shaped", false);
			List<RecipeChoice> choiceList = recipe.getChoiceList();
			for (int i = 0; i < choiceList.size(); i++) {
				RecipeChoice choice = choiceList.get(i);
				saveChoice(key + ".choices." + i, choice);
			}
		} else if (r instanceof FurnaceRecipe) {
			FurnaceRecipe recipe = (FurnaceRecipe) r;
			key = recipe.getKey().getKey();
			saveChoice(key + ".ingredient", recipe.getInputChoice());
			recipeConfig.set(key + ".experience", recipe.getExperience());
			recipeConfig.set(key + ".cookingTime", recipe.getCookingTime());
		}
		recipeConfig.set(key + ".result", r.getResult());
		try {
			recipeConfig.save(recipeFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Recipe loadRecipe(String key) {
		NamespacedKey recipeKey = new NamespacedKey(this, key);
		ItemStack result = recipeConfig.getItemStack(key + ".result");
		if (recipeConfig.getBoolean(key + ".shaped")) {
			ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
			recipe.shape("012", "345", "678");
			for (int i = 0; i < 9; i++) {
				RecipeChoice choice = loadChoice(key + ".choices." + i);
				if (choice == null) continue;
				recipe.setIngredient((char)(i + '0'), choice);
			}
			return recipe;
		} else {
			int cookingTime = recipeConfig.getInt(key + ".cookingTime", -1);
			if (cookingTime == -1) {
				ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, result);
				for (String choiceKey : recipeConfig.getConfigurationSection(key + ".choices").getKeys(false)) {
					RecipeChoice choice = loadChoice(key + ".choices." + choiceKey);
					if (choice == null) continue;
					recipe.addIngredient(choice);
				}
				return recipe;
			} else {
				FurnaceRecipe recipe = new FurnaceRecipe(recipeKey, result, loadChoice(key + ".ingredient"), (float) recipeConfig.getDouble(key + ".experience"), cookingTime);
				return recipe;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void saveChoice(String path, RecipeChoice choice) {
		if (choice == null) return;
		if (choice instanceof ExactChoice) {
			recipeConfig.set(path + ".exact", true);
			recipeConfig.set(path + ".items", ((ExactChoice) choice).getChoices());
		} else {
			recipeConfig.set(path + ".exact", false);
			recipeConfig.set(path + ".items", ((MaterialChoice) choice).getChoices().stream().map(Material::toString).collect(Collectors.toList()));
		}
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public RecipeChoice loadChoice(String path) {
		if (recipeConfig.getList(path + ".items") == null) return null;
		if (recipeConfig.getBoolean(path + ".exact")) {
			List<ItemStack> possibleStacks = (List<ItemStack>) recipeConfig.getList(path + ".items");
			return new ExactChoice(possibleStacks);
		} else {
			List<Material> possibleMaterials = recipeConfig.getStringList(path + ".items").stream().map(Material::getMaterial).collect(Collectors.toList());
			return new MaterialChoice(possibleMaterials);
		}
	}
	
	public boolean sendMessage(CommandSender sender, String path) {
		sender.sendMessage(makeReadable(getLang().getString(path)));
		return true;
	}
	
	public String makeReadable(String input) {
		return input == null ? null : CMIChatColor.colorize(input);
	}
	
}
