package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;

public class LastOption extends DefaultSpawnOption {

    public LastOption(MMOLineConfig config) {
        super(config);
    }

    @Override
    public SpawnPoint getSpawnPoint(PlayerData playerData) {
        return playerData.getLastSpawnPoint();
    }

}
