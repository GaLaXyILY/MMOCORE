package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPointContext;

public class LastUsedOption extends DefaultSpawnOption {

    public LastUsedOption(MMOLineConfig config) {
        super(config);
    }

    @Override
    public SpawnPointContext getSpawnPointContext(PlayerData playerData) {
        return playerData.getLastSpawnPointContext();
    }

}
