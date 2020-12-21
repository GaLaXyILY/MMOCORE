package net.Indyuce.mmocore.api.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.SoundManager;

public class LootChest {
	private final ChestTier tier;
	private final LootChestRegion region;
	private final ReplacedBlock block;
	private final BukkitRunnable effectRunnable;
	private final long date = System.currentTimeMillis();

	/*
	 * instance generated when a loot chest is placed (as a bukkit block), and
	 * used to save the data of the block which has been replaced (can replace
	 * non-solid blocks)
	 */
	public LootChest(ChestTier tier, LootChestRegion region, Block block) {
		this.tier = tier;
		this.region = region;
		this.block = new ReplacedBlock(block);
		this.effectRunnable = tier.hasEffect() ? tier.getEffect().startNewRunnable(block.getLocation().add(.5, .5, .5)) : null;
	}

	public ChestTier getTier() {
		return tier;
	}

	public ReplacedBlock getBlock() {
		return block;
	}

	public LootChestRegion getRegion() {
		return region;
	}

	public boolean hasPlayerNearby() {
		for (Player player : block.loc.getWorld().getPlayers())
			if (player.getLocation().distanceSquared(block.loc) < 625)
				return true;
		return false;
	}

	public boolean shouldExpire() {
		return System.currentTimeMillis() - date > MMOCore.plugin.configManager.lootChestExpireTime;
	}

	public void unregister(boolean player) {

		/*
		 * if a player is responsible of closing the chest, close it with sound
		 */
		if (player) {
			MMOCore.plugin.soundManager.play(block.loc.getBlock(), SoundManager.SoundEvent.CLOSE_LOOT_CHEST);
			block.loc.getWorld().spawnParticle(Particle.CRIT, block.loc.clone().add(.5, .5, .5), 16, 0, 0, 0, .5);
			MMOCore.plugin.lootChests.unregister(this);
		}

		/*
		 * must clean block inventory before replacing block otherwise loots fly
		 * off and accumulate on the ground (+during dev phase)
		 */
		else
			((Chest) block.loc.getBlock().getState()).getBlockInventory().clear();

		block.restore();
		if (effectRunnable != null)
			effectRunnable.cancel();
	}

	public static class ReplacedBlock {
		private final Material material;
		private final BlockData data;
		private final Location loc;

		public ReplacedBlock(Block block) {
			this.material = block.getType();
			this.data = block.getBlockData();
			this.loc = block.getLocation();
		}

		public Location getLocoation() {
			return loc;
		}

		public boolean matches(Location loc) {
			return this.loc.getWorld().equals(loc.getWorld()) && this.loc.getBlockX() == loc.getBlockX() && this.loc.getBlockY() == loc.getBlockY()
					&& this.loc.getBlockZ() == loc.getBlockZ();
		}

		public void restore() {
			loc.getBlock().setType(material);
			loc.getBlock().setBlockData(data);
		}
	}
}
