package net.Indyuce.mmocore.manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobSkill;

public class SkillManager {
	private final Map<String, Skill> skills = new LinkedHashMap<>();

	/*
	 * skills are initialized when MMOCore enables but SkillManager must be
	 * instanced when MMOCore loads so that extra plugins can register skills
	 * before CLASSES are loaded
	 */
	public void reload() {

		if (skills.isEmpty())
			try {
				JarEntry entry;
				for (Enumeration<JarEntry> en = new JarFile(MMOCore.plugin.getJarFile()).entries(); en.hasMoreElements();)
					if ((entry = en.nextElement()).getName().startsWith("net/Indyuce/mmocore/skill/") && !entry.isDirectory()
							&& !entry.getName().contains("$"))
						register((Skill) Class.forName(entry.getName().replace("/", ".").replace(".class", "")).newInstance());
			} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
				exception.printStackTrace();
				MMOCore.log(Level.WARNING, "Could not load skills! Careful with player data :(");
			}

		if (!new File(MMOCore.plugin.getDataFolder() + "/skills").exists())
			new File(MMOCore.plugin.getDataFolder() + "/skills").mkdir();

		File mythicMobs = new File(MMOCore.plugin.getDataFolder() + "/skills/mythic-mobs");
		if (!mythicMobs.exists())
			mythicMobs.mkdir();

		/*
		 * load MythicMobs addon skills
		 */
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
			for (File file : mythicMobs.listFiles()) {
				try {
					register(new MythicMobSkill(file.getName().substring(0, file.getName().length() - 4).toUpperCase(),
							YamlConfiguration.loadConfiguration(file)));
				} catch (Exception exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load skill from " + file.getName() + ": " + exception.getMessage());
				}
			}

		for (Skill skill : getAll())
			if (!(skill instanceof MythicMobSkill)) {
				File file = new File(MMOCore.plugin.getDataFolder() + "/skills", skill.getLowerCaseId() + ".yml");
				ConfigFile config = new ConfigFile("/skills", skill.getLowerCaseId());

				if (!file.exists()) {
					config.getConfig().set("name", skill.getName());
					config.getConfig().set("lore", skill.getLore());

					/*
					 * it does support custom modeled items but it does not
					 * provide default configs for that.
					 */
					config.getConfig().set("material", skill.getIcon().getType().name());

					for (String mod : skill.getModifiers()) {
						LinearValue value = skill.getModifierInfo(mod);
						config.getConfig().set(mod + ".base", value.getBaseValue());
						config.getConfig().set(mod + ".per-level", value.getPerLevel());
						if (value.hasMax())
							config.getConfig().set(mod + ".max", value.getMax());
						if (value.hasMin())
							config.getConfig().set(mod + ".min", value.getMin());
					}
				}

				skill.update(config.getConfig());
				config.save();
			}
	}

	public void register(Skill skill) {
		skills.put(skill.getId().toUpperCase(), skill);
	}

	public Skill get(String id) {
		return skills.get(id.toUpperCase());
	}

	public boolean has(String id) {
		return skills.containsKey(id.toUpperCase());
	}

	public Collection<Skill> getAll() {
		return skills.values();
	}

	public Set<Skill> getActive() {
		return skills.values().stream().filter(skill -> !skill.isPassive()).collect(Collectors.toSet());
	}

	public Set<String> getKeys() {
		return skills.keySet();
	}
}
