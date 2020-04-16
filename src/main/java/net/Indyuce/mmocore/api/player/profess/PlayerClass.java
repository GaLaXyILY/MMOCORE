package net.Indyuce.mmocore.api.player.profess;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.AltChar;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.load.PostLoadObject;
import net.Indyuce.mmocore.api.player.ExpCurve;
import net.Indyuce.mmocore.api.player.profess.event.EventTrigger;
import net.Indyuce.mmocore.api.player.profess.resource.ManaDisplayOptions;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.profess.resource.ResourceHandler;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.api.util.math.particle.CastingParticle;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.version.VersionMaterial;

public class PlayerClass extends PostLoadObject {
	private final String name, id;
	private final List<String> description = new ArrayList<>(), attrDescription = new ArrayList<>();
	private final ItemStack icon;
	private final Map<ClassOption, Boolean> options = new HashMap<>();
	private final ManaDisplayOptions manaDisplay;
	private final int maxLevel, displayOrder;
	private final ExpCurve expCurve;

	private final Map<StatType, LinearValue> stats = new HashMap<>();
	private final Map<String, SkillInfo> skills = new LinkedHashMap<>();
	private final List<Subclass> subclasses = new ArrayList<>();

	private final Map<PlayerResource, ResourceHandler> resourceHandlers = new HashMap<>();
	private final Map<String, EventTrigger> eventTriggers = new HashMap<>();

	private final CastingParticle castParticle;

	public PlayerClass(String id, FileConfiguration config) {
		super(config);

		this.id = id.toUpperCase().replace("-", "_").replace(" ", "_");

		name = ChatColor.translateAlternateColorCodes('&', config.getString("display.name"));
		icon = MMOCoreUtils.readIcon(config.getString("display.item"));

		if (config.contains("display.texture"))
			if (icon.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
				ItemMeta meta = icon.getItemMeta();
				try {
					Field profileField = meta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					GameProfile gp = new GameProfile(UUID.randomUUID(), null);
					gp.getProperties().put("textures", new Property("textures", config.getString("display.texture")));
					profileField.set(meta, gp);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not apply playerhead texture: " + exception.getMessage());
				}
				icon.setItemMeta(meta);
			} else
				MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not add player head texture. The item is not a playerhead!");

		for (String string : config.getStringList("display.lore"))
			description.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', string));
		for (String string : config.getStringList("display.attribute-lore"))
			attrDescription.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', string));
		manaDisplay = new ManaDisplayOptions(config.getConfigurationSection("mana"));
		maxLevel = config.getInt("max-level");
		displayOrder = config.getInt("display.order");

		expCurve = config.contains("exp-curve")
				? MMOCore.plugin.experience.getOrThrow(config.get("exp-curve").toString().toLowerCase().replace("_", "-").replace(" ", "-"))
				: ExpCurve.DEFAULT;

		if (config.contains("attributes"))
			for (String key : config.getConfigurationSection("attributes").getKeys(false))
				try {
					stats.put(StatType.valueOf(key.toUpperCase().replace("-", "_")),
							new LinearValue(config.getConfigurationSection("attributes." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not load stat info '" + key + "': " + exception.getMessage());
				}

		if (config.contains("skills"))
			for (String key : config.getConfigurationSection("skills").getKeys(false))
				try {
					Validate.isTrue(MMOCore.plugin.skillManager.has(key), "Could not find skill " + key);
					skills.put(key.toUpperCase(), MMOCore.plugin.skillManager.get(key).newSkillInfo(config.getConfigurationSection("skills." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not load skill info '" + key + "': " + exception.getMessage());
				}

		castParticle = config.contains("cast-particle") ? new CastingParticle(config.getConfigurationSection("cast-particle"))
				: new CastingParticle(Particle.SPELL_INSTANT);

		if (config.contains("options"))
			for (String key : config.getConfigurationSection("options").getKeys(false))
				try {
					setOption(ClassOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_")), config.getBoolean("options." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not read class option from '" + key + "'");
				}

		if (config.contains("main-exp-sources"))
			for (String key : config.getStringList("main-exp-sources"))
				try {
					ExperienceSource<?> source = MMOCore.plugin.loadManager.loadExperienceSource(new MMOLineConfig(key), null);
					source.setClass(this);
					MMOCore.plugin.professionManager.registerExpSource(source);
				} catch (MMOLoadException exception) {
					exception.printConsole("PlayerClasses:" + id, "exp source");
				}

		if (config.contains("triggers"))
			for (String key : config.getConfigurationSection("triggers").getKeys(false)) {
				try {
					String format = key.toLowerCase().replace("_", "-").replace(" ", "-");
					eventTriggers.put(format, new EventTrigger(format, config.getStringList("triggers." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] " + exception.getMessage());
				}
			}

		/*
		 * must make sure all the resourceHandlers are registered when the
		 * placer class is initialized.
		 */
		for (PlayerResource resource : PlayerResource.values()) {
			if (config.isConfigurationSection("resource." + resource.name().toLowerCase()))
				try {
					resourceHandlers.put(resource,
							new ResourceHandler(resource, config.getConfigurationSection("resource." + resource.name().toLowerCase())));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not load special resource regen for " + resource.name() + ": "
							+ exception.getMessage());
					resourceHandlers.put(resource, new ResourceHandler(resource));
				}
			else
				resourceHandlers.put(resource, new ResourceHandler(resource));
		}
	}

	/*
	 * used to generate display class
	 */
	public PlayerClass(String id, String name, Material material) {
		super(null);

		this.id = id;
		this.name = name;
		manaDisplay = new ManaDisplayOptions(ChatColor.BLUE, "Mana", AltChar.listSquare.charAt(0));
		maxLevel = 0;
		displayOrder = 0;
		expCurve = ExpCurve.DEFAULT;
		castParticle = new CastingParticle(Particle.SPELL_INSTANT);

		this.icon = new ItemStack(material);
		setOption(ClassOption.DISPLAY, false);
		setOption(ClassOption.DEFAULT, false);

		for (PlayerResource resource : PlayerResource.values())
			resourceHandlers.put(resource, new ResourceHandler(resource));
	}

	@Override
	protected void whenPostLoaded(FileConfiguration config) {
		if (config.contains("subclasses"))
			for (String key : config.getConfigurationSection("subclasses").getKeys(false))
				subclasses.add(new Subclass(MMOCore.plugin.classManager.get(key.toUpperCase().replace("-", "_").replace(" ", "_")),
						config.getInt("subclasses." + key)));
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ManaDisplayOptions getManaDisplay() {
		return manaDisplay;
	}

	public ResourceHandler getHandler(PlayerResource resource) {
		return resourceHandlers.get(resource);
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public ExpCurve getExpCurve() {
		return expCurve;
	}

	public ItemStack getIcon() {
		return icon.clone();
	}

	public CastingParticle getCastParticle() {
		return castParticle;
	}

	public List<String> getDescription() {
		return description;
	}

	public List<String> getAttributeDescription() {
		return attrDescription;
	}

	public void setOption(ClassOption option, boolean value) {
		options.put(option, value);
	}

	public boolean hasOption(ClassOption option) {
		return options.containsKey(option) ? options.get(option) : option.getDefault();
	}

	public void setStat(StatType type, double base, double perLevel) {
		stats.put(type, new LinearValue(base, perLevel));
	}

	public double calculateStat(StatType stat, int level) {
		return getStatInfo(stat).calculate(level);
	}

	public List<Subclass> getSubclasses() {
		return subclasses;
	}

	public boolean hasSkill(Skill skill) {
		return skills.containsKey(skill.getId());
	}

	public SkillInfo getSkill(Skill skill) {
		return getSkill(skill.getId());
	}

	public SkillInfo getSkill(String id) {
		return skills.get(id.toUpperCase());
	}

	public Set<String> getEventTriggers() {
		return eventTriggers.keySet();
	}

	public boolean hasEventTriggers(String name) {
		return eventTriggers.containsKey(name);
	}

	public EventTrigger getEventTriggers(String name) {
		return eventTriggers.get(name);
	}

	public Collection<SkillInfo> getSkills() {
		return skills.values();
	}

	private LinearValue getStatInfo(StatType type) {
		return stats.containsKey(type) ? stats.get(type) : type.getDefault();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof PlayerClass && ((PlayerClass) obj).id.equals(id);
	}
}
