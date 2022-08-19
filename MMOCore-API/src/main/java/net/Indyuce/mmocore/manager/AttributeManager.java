package net.Indyuce.mmocore.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeManager implements MMOCoreManager {
	private final Map<String, PlayerAttribute> map = new HashMap<>();

	@Nullable
	public PlayerAttribute get(String id) {
		return map.get(id);
	}

	public boolean has(String id) {
		return map.containsKey(id);
	}

	@NotNull
	public Collection<PlayerAttribute> getAll() {
		return map.values();
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			map.clear();

		ConfigFile config = new ConfigFile("attributes");
		for (String key : config.getConfig().getKeys(false))
			try {
				String path = key.toLowerCase().replace("_", "-").replace(" ", "-");
				map.put(path, new PlayerAttribute(config.getConfig().getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load attribute '" + key + "': " + exception.getMessage());
			}
	}
}
