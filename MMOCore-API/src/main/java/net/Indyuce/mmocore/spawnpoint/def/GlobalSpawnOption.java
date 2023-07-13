package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;

public class GlobalSpawnOption extends DefaultSpawnOption {

    public GlobalSpawnOption(MMOLineConfig config) {
        super(config);
    }

    @Override
    public Location getSpawnLocation(PlayerData playerData) {
        return MMOCore.plugin.spawnPointManager.getGlobalSpawn();
    }

    @Override
    public void whenRespawn() {

    }
}
