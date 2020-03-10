package net.Indyuce.mmocore.api.experience;

import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.mmogroup.mmolib.MMOLib;

public class Profession {
	private final String id, name, expCurve;

	private final LinearValue experience;
	
	/*
	 * removed when loaded
	 */
	private FileConfiguration config;

	public Profession(String id, FileConfiguration config) {
		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.config = config;

		this.name = config.getString("name");
		Validate.notNull(name, "Could not load name");

		expCurve = config.getString("exp-curve", "levels");
		experience = new LinearValue(config.getConfigurationSection("experience"));

		if (config.contains("exp-sources"))
			for (String key : config.getStringList("exp-sources"))
				try {
					MMOCore.plugin.professionManager.registerExpSource(MMOCore.plugin.loadManager.loadExperienceSource(new MMOLineConfig(key), this));
				} catch (MMOLoadException exception) {
					exception.printConsole("Professions", "exp source");
				}
	}

	/*
	 * drop tables must be loaded after professions are initialized
	 */
	public void loadOptions() {

		if (config.contains("on-fish"))
			MMOCore.plugin.fishingManager.loadDropTables(config.getConfigurationSection("on-fish"));

		if (config.contains("on-mine"))
			MMOCore.plugin.mineManager.loadDropTables(config.getConfigurationSection("on-mine"));

		if (config.contains("alchemy-experience")) {

			MMOCore.plugin.alchemyManager.splash = 1 + config.getDouble("alchemy-experience.special.splash") / 100;
			MMOCore.plugin.alchemyManager.lingering = 1 + config.getDouble("alchemy-experience.special.lingering") / 100;
			MMOCore.plugin.alchemyManager.extend = 1 + config.getDouble("alchemy-experience.special.extend") / 100;
			MMOCore.plugin.alchemyManager.upgrade = 1 + config.getDouble("alchemy-experience.special.upgrade") / 100;

			for (String key : config.getConfigurationSection("alchemy-experience.effects").getKeys(false))
				try {
					PotionType type = PotionType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					MMOCore.plugin.alchemyManager.registerBaseExperience(type, config.getDouble("alchemy-experience.effects." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[Professions:" + id + "] Could not read potion type from " + key);
				}
		}

		if (config.contains("base-enchant-exp"))
			for (String key : config.getConfigurationSection("base-enchant-exp").getKeys(false))
				try {
					Enchantment enchant = MMOLib.plugin.getVersion().getWrapper().getEnchantmentFromString(key.toLowerCase().replace("-", "_"));
					MMOCore.plugin.enchantManager.registerBaseExperience(enchant, config.getDouble("base-enchant-exp." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[Professions:" + id + "] Could not read enchant from " + key);
				}

		if (config.contains("repair-exp"))
			for (String key : config.getConfigurationSection("repair-exp").getKeys(false))
				try {
					Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					MMOCore.plugin.smithingManager.registerBaseExperience(material, config.getDouble("repair-exp." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[Professions:" + id + "] Could not read material from " + key);
				}

		// if (config.contains("effect-weight"))
		// for (String key :
		// config.getConfigurationSection("effect-weight").getKeys(false))
		// try {
		// MMOCore.plugin.alchemyManager.registerEffectWeight(PotionEffectType.getByName(key.toUpperCase().replace("-",
		// "_").replace(" ", "_")), config.getDouble("effect-weight." + key));
		// } catch (IllegalArgumentException exception) {
		// MMOCore.log(Level.WARNING, "[Professions:" + id + "] Could not read
		// potion effect type from " + key);
		// }

		config = null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEXPCurve() {
		return expCurve;
	}
	
	public int calculateExperience(int x) {
		return (int) experience.calculate(x);
	}

	public LinearValue getExperience() {
		return experience;
	}
}
