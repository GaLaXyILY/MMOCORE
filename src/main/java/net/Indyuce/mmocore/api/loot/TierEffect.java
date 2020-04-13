package net.Indyuce.mmocore.api.loot;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.math.particle.ChestParticleEffect;

public class TierEffect {
	private final ChestParticleEffect type;
	private final Particle particle;
	private final int period;

	public TierEffect(ConfigurationSection config) {
		Validate.notNull(config, "Could not load tier config");
		type = ChestParticleEffect.valueOf(config.getString("type", "OFFSET").toUpperCase().replace("-", "_").replace(" ", "_"));
		particle = Particle.valueOf(config.getString("particle", "FLAME").toUpperCase().replace("-", "_").replace(" ", "_"));
		period = Math.max(20, config.getInt("period", 5 * 20));
	}

	public void play(Location loc) {
		type.play(loc, particle);
	}

	public BukkitRunnable startNewRunnable(Location loc) {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				type.play(loc, particle);
			}
		};
		runnable.runTaskTimer(MMOCore.plugin, 0, period);
		return runnable;
	}
}