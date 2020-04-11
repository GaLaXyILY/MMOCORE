package net.Indyuce.mmocore.manager.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;

public abstract class GuildDataManager {
	protected final Map<String, Guild> guilds = new HashMap<>();

	public Guild newRegisteredGuild(UUID owner, String name, String tag) {
		Guild guild = new Guild(owner, name, tag);
		registerGuild(guild);
		return guild;
	}

	public void registerGuild(Guild guild) {
		guilds.put(guild.getId(), guild);
	}

	public boolean isRegistered(Guild guild) {
		return guilds.containsValue(guild);
	}

	public boolean isRegistered(String tag) {
		return guilds.containsKey(tag);
	}

	public void unregisterGuild(Guild guild) {
		guild.getMembers().forEach(member -> guild.removeMember(member, true));
		// guild.getMembers().clear();
		guilds.remove(guild.getId());
		delete(guild);
	}

	public Guild stillInGuild(UUID uuid, String id) {
		Guild guild = getGuild(id);
		if (guild != null && guild.getMembers().has(uuid))
			return guild;
		return null;
	}

	public Guild getGuild(String guild) {
		return guilds.get(guild);
	}

	public Collection<Guild> getAll() {
		return guilds.values();
	}

	public void reload() {
		for (Guild guild : guilds.values())
			save(guild);
		guilds.clear();
		load();
		config = new GuildConfiguration();
	}

	public abstract void save(Guild guild);

	public abstract void load();

	public abstract void delete(Guild guild);

	// Shitty code for loading config values for guilds.
	private GuildConfiguration config;

	public GuildConfiguration getConfig() {
		if (config == null)
			config = new GuildConfiguration();
		return config;
	}

	public class GuildConfiguration {
		private final String prefix;
		private final boolean uppercaseTags;
		private final NamingRules tagRules, nameRules;

		public GuildConfiguration() {
			FileConfiguration config = new ConfigFile("guilds").getConfig();

			this.prefix = config.getString("chat-prefix", "*");
			this.uppercaseTags = config.getBoolean("uppercase-tags", true);
			this.tagRules = new NamingRules(config.getConfigurationSection("rules.tag"));
			this.nameRules = new NamingRules(config.getConfigurationSection("rules.name"));
		}

		public String getPrefix() {
			return prefix;
		}

		public boolean shouldUppercaseTags() {
			return uppercaseTags;
		}

		public NamingRules getTagRules() {
			return tagRules;
		}

		public NamingRules getNameRules() {
			return nameRules;
		}

		public class NamingRules {
			private final String regex;
			private final int min, max;

			public NamingRules(ConfigurationSection config) {
				regex = config.getString("matches", "[a-zA-Z-_!?]+");
				min = config.getInt("min-length", 3);
				max = config.getInt("max-length", 5);
			}

			public String getRegex() {
				return regex;
			}

			public int getMin() {
				return min;
			}

			public int getMax() {
				return max;
			}
		}
	}
}
