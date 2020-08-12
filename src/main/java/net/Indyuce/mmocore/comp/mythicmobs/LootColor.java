package net.Indyuce.mmocore.comp.mythicmobs;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;

public class LootColor extends BukkitRunnable {
	private final Item item;
	private final Color color;

	private int j = 0;

	public LootColor(Item item, Color color) {
		this.item = item;
		this.color = color;
		
		runTaskTimer(MMOCore.plugin, 0, 1);
	}

	@Override
	public void run() {
		if (j++ > 100 || item.isDead() || item.isOnGround()) {
			cancel();
			return;
		}

		item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation(), 1, new Particle.DustOptions(color, 1.3f));
	}
}
