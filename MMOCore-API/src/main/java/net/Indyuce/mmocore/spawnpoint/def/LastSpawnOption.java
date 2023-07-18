package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPointContext;

public class LastSpawnOption extends DefaultSpawnOption {

    public LastSpawnOption(MMOLineConfig config) {
        super(config);
    }

    @Override
    public SpawnPointContext getSpawnPointContext(PlayerData playerData) {
        return playerData.getLastSpawnPointContext();
    }

}
